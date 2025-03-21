package main.java.zenit.filesystem;

import main.java.zenit.ui.FileTab;
import main.java.zenit.ui.MainController;

import java.io.File;

public class AutoSave implements Runnable {

    private boolean isRunning = true;
    private MainController mainController;
    private int restartCounter = 0;

    public AutoSave(MainController mainController) {
        this.mainController = mainController;
        new Thread(this).start();
    }

    @Override
    public void run() {
        while(isRunning) {
            try {
                Thread.sleep(30000);

                if(mainController == null) {
                    continue;
                }

                if(mainController.changesHaveBeenMade()) {
                    FileTab tab = mainController.getSelectedTab();
                    if(tab == null) {
                        continue;
                    }

                    File file = tab.getFile();
                    if(file != null) {
                        String text = mainController.getZenCodeAreaContent();
                        if(text != null && !text.isEmpty()) {
                            FileController.writeFile(file, text);
                        }
                    }
                }
            } catch (InterruptedException e) {
                System.out.println("AutoSave crashed, trying to recover");
                if(restartCounter > 5) {
                    System.out.println("AutoSave crashed too many times, stopping");
                    isRunning = false;
                }
                e.printStackTrace();
            }
        }
    }

    public void stop() {
        isRunning = false;
    }

    public boolean isRunning() {
        return isRunning;
    }
}
