package settings;

/**
 * This enumerable type lists the various application-specific property types listed in the initial set of properties to
 * be loaded from the workspace properties <code>xml</code> file specified by the initialization parameters.
 *
 * @author Ritwik Banerjee
 * @see vilij.settings.InitializationParams
 */
public enum AppPropertyTypes {

    /* user interface icon file names */
    SCREENSHOT_ICON,

    SETTINGSS_ICON,
    RUN_ICON,

    NO_TEXT_TO_DISPLAY,
    NO_TEXT_TO_DISPLAY_TITLE,
    /* tooltips for user interface buttons */
    SCREENSHOT_TOOLTIP,

    INVALID_DATA_TITLE,


    /* application-specific message titles */
    SAVE_UNSAVED_WORK_TITLE,

    /* application-specific messages */
    SAVE_UNSAVED_WORK,

    /* application-specific parameters */
    DATA_FILE_EXT,
    DATA_FILE_EXT_DESC,

}
