import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.List;

import static java.nio.file.StandardOpenOption.CREATE;

public class TagExtractorFrame extends JFrame
{
    JPanel mainPnl, titlePnl, displayPnl, cmdPnl, filePnl;
    JLabel titleLbl, sourceLbl, wordLbl, sourceFileLbl, wordFileLbl, savedLbl;
    JScrollPane scroller;
    JTextArea freqTA;
    JButton quitBtn, sourceBtn, wordBtn, runBtn, saveBtn;
    boolean source = false, word = false;
    String[] sourceWords;
    List<String> sourceWordsList;
    ArrayList<String> stopWords = new ArrayList<>(), sourceWordsArrayList = new ArrayList<>();
    Map<String, Integer> wordFreq;

    public TagExtractorFrame() throws HeadlessException
    {
        setTitle("Tag Extractor");
        Toolkit kit = Toolkit.getDefaultToolkit();
        Dimension screenSize = kit.getScreenSize();
        int scrnHeight = screenSize.height;
        int scrnWidth = screenSize.width;
        setSize(scrnWidth*3/4, scrnHeight*3/4);
        setLocation(scrnWidth/8, scrnHeight/8);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        mainPnl = new JPanel();
        mainPnl.setLayout(new BorderLayout());
        add(mainPnl);

        createTitlePanel();
        createCommandPanel();
        createDisplayPanel();

        setVisible(true);
    }
    private void createCommandPanel()
    {
        cmdPnl = new JPanel();
        cmdPnl.setLayout(new GridLayout(1,3));

        quitBtn = new JButton("Quit");
        quitBtn.setFont(new Font("Bold", Font.BOLD, 18));

        runBtn = new JButton("Run");
        runBtn.setFont(new Font("Bold", Font.BOLD, 18));
        runBtn.setEnabled(false);

        saveBtn = new JButton("Save");
        saveBtn.setFont(new Font("Bold", Font.BOLD, 18));
        saveBtn.setEnabled(false);

        quitBtn.addActionListener((ActionEvent ae) ->
        {
            String quit = "Are you sure you want to quit?";
            if (JOptionPane.showConfirmDialog(null, quit,"Quit", JOptionPane.YES_NO_OPTION)==0) {
                System.exit(0);
            }
        });
        runBtn.addActionListener((ActionEvent ae) ->
        {
            freqTA.setText("");
            freqTA.append(
                " Word                Frequency" + "\n ============================="
            );
            runTagExtractor();
            for (String key : wordFreq.keySet()) {
                Integer value = wordFreq.get(key);
                freqTA.append("\n " + String.format("%-20s", key) + String.format("%-9d", value));
            }
            savedLbl.setVisible(false);
            saveBtn.setEnabled(true);
        });
        saveBtn.addActionListener((ActionEvent ae) ->
        {
            String fileName = JOptionPane.showInputDialog("Please enter the desired file name.\nDo not include a file extension.\nThe file will be saved as a .txt file\nIf the entered file name already exists, no file will be written.");

            File workingDirectory = new File(System.getProperty("user.dir"));
            Path file = Paths.get(workingDirectory.getPath() + "\\src\\" + fileName + ".txt");

            savedLbl.setText("File saved as " + file);

            try{
                OutputStream out =
                        new BufferedOutputStream(Files.newOutputStream(file, CREATE));
                BufferedWriter writer =
                        new BufferedWriter(new OutputStreamWriter(out));
                for(String key : wordFreq.keySet()){
                    Integer value = wordFreq.get(key);
                    writer.write(key);
                    writer.write(", ");
                    writer.write(value.toString());
                    writer.newLine();
                }
                writer.close();
                System.out.println("Data file written!");
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }

            savedLbl.setVisible(true);
        });

        cmdPnl.add(runBtn);
        cmdPnl.add(saveBtn);
        cmdPnl.add(quitBtn);

        mainPnl.add(cmdPnl, BorderLayout.SOUTH);
    }
    private void createTitlePanel()
    {
        titlePnl = new JPanel();

        titleLbl = new JLabel("Tag Extractor", JLabel.CENTER);
        titleLbl.setVerticalTextPosition(JLabel.TOP);
        titleLbl.setHorizontalTextPosition(JLabel.CENTER);
        titleLbl.setFont(new Font("Bold Italic", Font.BOLD | Font.ITALIC, 36));

        titlePnl.add(titleLbl);

        mainPnl.add(titlePnl, BorderLayout.NORTH);
    }
    private void createDisplayPanel()
    {
        displayPnl = new JPanel();
        displayPnl.setLayout(new GridLayout(1,2));
        displayPnl.setBorder(new EmptyBorder(50,50,50,50));

        createFilePanel();
        displayPnl.add(filePnl);

        freqTA = new JTextArea(10,40);
        freqTA.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 14));
        freqTA.setEditable(false);
        scroller = new JScrollPane(freqTA);

        displayPnl.add(scroller);

        mainPnl.add(displayPnl);
    }
    private void createFilePanel()
    {
        filePnl = new JPanel();
        filePnl.setLayout(new BoxLayout(filePnl, BoxLayout.Y_AXIS));

        sourceLbl = new JLabel("Source File:");
        sourceFileLbl = new JLabel("No source file chosen.");
        sourceFileLbl.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 14));
        sourceBtn = new JButton("Choose Source File");

        sourceBtn.addActionListener((ActionEvent ae) ->
        {
            JFileChooser chooser = new JFileChooser();
            File selectedFile;
            String rec = "";

            sourceWordsArrayList.clear();

            try
            {
                File workingDirectory = new File(System.getProperty("user.dir"));
                chooser.setCurrentDirectory(workingDirectory);

                if(chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION)
                {
                    selectedFile = chooser.getSelectedFile();
                    Path file = selectedFile.toPath();
                    InputStream in = new BufferedInputStream(Files.newInputStream(file, CREATE));
                    BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                    while(reader.ready())
                    {
                        rec = reader.readLine();
                        sourceWords = rec.toLowerCase().split("[ 0-9,.!?:;$&()/_<>—–£#%*^~“”\\[\\]\"\n-]");
                        sourceWordsList = Arrays.asList(sourceWords);
                        sourceWordsArrayList.addAll(sourceWordsList);
                    }
                    reader.close();
                    sourceFileLbl.setText(String.valueOf(selectedFile));
                    source = true;
                    if (source&&word)
                    {
                        runBtn.setEnabled(true);
                    }
                }
            }
            catch (FileNotFoundException e)
            {
                System.out.println("File not found!!!");
                e.printStackTrace();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
            savedLbl.setVisible(false);
        });

        wordLbl = new JLabel("Noise/Stop Word File:");
        wordFileLbl = new JLabel("No noise/stop word file chosen.");
        wordFileLbl.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 14));
        wordBtn = new JButton("Choose Noise/Stop Word File");

        wordBtn.addActionListener((ActionEvent ae) ->
        {
            JFileChooser chooser = new JFileChooser();
            File selectedFile;
            String rec = "";

            stopWords.clear();

            try
            {
                File workingDirectory = new File(System.getProperty("user.dir"));
                chooser.setCurrentDirectory(workingDirectory);

                if(chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION)
                {
                    selectedFile = chooser.getSelectedFile();
                    Path file = selectedFile.toPath();
                    InputStream in = new BufferedInputStream(Files.newInputStream(file, CREATE));
                    BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                    while(reader.ready())
                    {
                        rec = reader.readLine();
                        stopWords.add(rec);
                    }
                    reader.close();
                    wordFileLbl.setText(String.valueOf(selectedFile));
                    word = true;
                    if (source&&word)
                    {
                        runBtn.setEnabled(true);
                    }
                }
            }
            catch (FileNotFoundException e)
            {
                System.out.println("File not found!!!");
                e.printStackTrace();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
            savedLbl.setVisible(false);
        });

        savedLbl = new JLabel("");
        savedLbl.setVisible(false);

        filePnl.add(sourceLbl);
        filePnl.add(sourceFileLbl);
        filePnl.add(sourceBtn);
        filePnl.add(new JLabel(" "));
        filePnl.add(wordLbl);
        filePnl.add(wordFileLbl);
        filePnl.add(wordBtn);
        filePnl.add(new JLabel(" "));
        filePnl.add(savedLbl);
    }
    private void runTagExtractor()
    {
        wordFreq = new HashMap<>();
        for (String source : sourceWordsArrayList) {
            if (!stopWords.contains(source)&&!source.isEmpty()) {
                if (wordFreq.containsKey(source)) {
                    Integer newValue = wordFreq.get(source) + 1;
                    wordFreq.put(source, newValue);
                } else {
                    wordFreq.put(source, 1);
                }
            }
        }
    }
}
