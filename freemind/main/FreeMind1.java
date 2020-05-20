package freemind.main;

import accessories.plugins.ExportWithXSLT;
import freemind.controller.Controller;
import freemind.modes.ModeController;

import javax.annotation.PostConstruct;
import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.io.File;
import java.util.Properties;

public class FreeMind1 extends FreeMind {
    public FreeMind1(Properties pDefaultPreferences, Properties pUserPreferences, File pAutoPropertiesFile) {
        super(pDefaultPreferences, pUserPreferences, pAutoPropertiesFile);
    }


    public static void main(final String[] args,
                            Properties pDefaultPreferences, Properties pUserPreferences,
                            File pAutoPropertiesFile) {
        final FreeMind1 frame = new FreeMind1(pDefaultPreferences,
                pUserPreferences, pAutoPropertiesFile);
        IFreeMindSplash splash = null;
        frame.checkForAnotherInstance(args);
        frame.initServer();
        final FeedBack feedBack;
        // change here, if you don't like the splash
        if (true) {
            splash = new FreeMindSplashModern(frame);
            splash.setVisible(true);
            feedBack = splash.getFeedBack();
            frame.mWindowIcon = splash.getWindowIcon();
        } else {
            feedBack = new FeedBack() {
                int value = 0;

                public int getActualValue() {
                    return value;
                }

                public void increase(String messageId,
                                     Object[] pMessageParameters) {
                    progress(getActualValue() + 1, messageId,
                            pMessageParameters);
                }

                public void progress(int act, String messageId,
                                     Object[] pMessageParameters) {
                    frame.logger.info("Beginnig task:" + messageId);
                }

                public void setMaximumValue(int max) {
                }
            };
            frame.mWindowIcon = new ImageIcon(
                    frame.getResource("images/FreeMindWindowIcon.png"));
        }
        feedBack.setMaximumValue(10 + frame.getMaximumNumberOfMapsToLoad(args));
        frame.init(feedBack);

        feedBack.increase("FreeMind.progress.startCreateController", null);
        final ModeController ctrl = frame.createModeController(args);

        feedBack.increase(FREE_MIND_PROGRESS_LOAD_MAPS, null);

        frame.loadMaps(args, ctrl, feedBack);

        Tools.waitForEventQueue();
        feedBack.increase("FreeMind.progress.endStartup", null);
        // focus fix after startup.
        frame.addWindowFocusListener(new WindowFocusListener() {

            public void windowLostFocus(WindowEvent e) {
            }

            public void windowGainedFocus(WindowEvent e) {
                frame.getController().obtainFocusForSelected();
                frame.removeWindowFocusListener(this);
            }
        });
        frame.setVisible(true);
        if (splash != null) {
            splash.setVisible(false);
        }
        frame.fireStartupDone();
        frame.automate();
    }


    @PostConstruct
    public void automate() {
        Controller controller = getController();
        controller.setZoom(1.5F);

        ModeController modeController = controller.getModeController();
        ExportWithXSLT ex = (ExportWithXSLT) modeController.getHookFactory().createModeControllerHook("accessories/plugins/ExportWithXSLT_HTML3.properties");
        ex.setController(modeController);
        String file = modeController.getMap().getFile().getAbsolutePath().replaceFirst("\\.[^.]*?$", "") + ".html";
        try {
            ex.transform(new File(file));
        } catch(Exception e) {
            freemind.main.Resources.getInstance().logException(e);
        }
        System.exit(0);
    }
}
