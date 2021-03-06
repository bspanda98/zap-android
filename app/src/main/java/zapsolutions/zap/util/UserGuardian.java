package zapsolutions.zap.util;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.CheckBox;

import zapsolutions.zap.R;


/**
 * The UserGuardian is designed to help inexperienced people keeping their bitcoin safe.
 * Use this class to show security warnings whenever the user does something that harms
 * his security or privacy.
 * To avoid too many popups, these messages have a "do not show again" option.
 * <p>
 * Please note that a dialog which will not be shown (do not show again checked) executes
 * the callback like it does when "ok" is pressed.
 */
public class UserGuardian {

    private static final String DIALOG_COPY_TO_CLIPBOARD = "guardianCopyToClipboard";
    private static final String DIALOG_PASTE_FROM_CLIPBOARD = "guardianPasteFromClipboard";
    private static final String DIALOG_DISABLE_SCRAMBLED_PIN = "guardianDisableScrambledPin";
    private static final String DIALOG_DISABLE_SCREEN_PROTECTION = "guardianDisableScreenProtection";
    private static final String DIALOG_HIGH_ONCHAIN_FEE = "guardianHighOnCainFees";
    private static final String DIALOG_OLD_EXCHANGE_RATE = "guardianOldExchangeRate";
    private static final String DIALOG_TOO_MUCH_MONEY = "guardianTooMuchMoney";
    private static final String DIALOG_MAINNET_NOT_READY = "guardianMainnetNotReady";
    private static final String DIALOG_REMOTE_CONNECT = "guardianRemoteConnect";
    private static final String DIALOG_BLOCK_EXPLORER = "guardianBlockExplorer";

    public static final int CLIPBOARD_DATA_TYPE_ONCHAIN = 0;
    public static final int CLIPBOARD_DATA_TYPE_LIGHTNING = 1;

    private final Context mContext;
    private OnGuardianConfirmedListener mListener;
    private String mCurrentDialogName;
    private CheckBox mDontShowAgain;

    public UserGuardian(Context ctx) {
        mContext = ctx;
    }

    public UserGuardian(Context ctx, OnGuardianConfirmedListener listener) {
        mContext = ctx;
        mListener = listener;
    }

    /**
     * Reset all "do not show again" selections.
     */
    public static void reenableAllSecurityWarnings() {
        PrefsUtil.edit()
                .putBoolean(DIALOG_COPY_TO_CLIPBOARD, true)
                .putBoolean(DIALOG_PASTE_FROM_CLIPBOARD, true)
                .putBoolean(DIALOG_DISABLE_SCRAMBLED_PIN, true)
                .putBoolean(DIALOG_DISABLE_SCREEN_PROTECTION, true)
                .putBoolean(DIALOG_HIGH_ONCHAIN_FEE, true)
                .putBoolean(DIALOG_OLD_EXCHANGE_RATE, true)
                .putBoolean(DIALOG_TOO_MUCH_MONEY, true)
                .putBoolean(DIALOG_MAINNET_NOT_READY, true)
                .putBoolean(DIALOG_REMOTE_CONNECT, true)
                .putBoolean(DIALOG_BLOCK_EXPLORER, true)
                .apply();
    }

    /**
     * Warn the user about security issues when copying stuff to clipboard.
     * Also provide the user with a check string to secure himself
     *
     * @param data the data that is copied to clipboard
     */
    public void securityCopyToClipboard(String data, int type) {
        mCurrentDialogName = DIALOG_COPY_TO_CLIPBOARD;

        String compareString;
        String message = "";
        switch (type) {
            case CLIPBOARD_DATA_TYPE_ONCHAIN:
                if (data.length() > 15) {
                    compareString = data.substring(0, 15) + " ...";
                    message = mContext.getResources().getString(R.string.guardian_copyToClipboard_onChain, compareString);
                }
                break;
            case CLIPBOARD_DATA_TYPE_LIGHTNING:
                if (data.length() > 15) {
                    compareString = "... " + data.substring(data.length() - 8);
                    message = mContext.getResources().getString(R.string.guardian_copyToClipboard_lightning, compareString);
                }
                break;
        }

        AlertDialog.Builder adb = createDontShowAgainDialog(true);
        adb.setMessage(message);
        showGuardianDialog(adb);
    }

    /**
     * Warn the user about pasting a payment request from clipboard.
     */
    public void securityPasteFromClipboard() {
        mCurrentDialogName = DIALOG_PASTE_FROM_CLIPBOARD;
        AlertDialog.Builder adb = createDontShowAgainDialog(false);
        adb.setMessage(R.string.guardian_pasteFromClipboard);
        showGuardianDialog(adb);
    }

    /**
     * Warn the user about using the wallet on mainnet, while it is still not secure.
     */
    public void securityMainnetNotReady() {
        mCurrentDialogName = DIALOG_MAINNET_NOT_READY;
        AlertDialog.Builder adb = createDontShowAgainDialog(false);
        adb.setMessage(R.string.guardian_notReadyForMainnet);
        showGuardianDialog(adb);
    }

    /**
     * Warn the user to not disable scrambled pin input.
     */
    public void securityScrambledPin() {
        mCurrentDialogName = DIALOG_DISABLE_SCRAMBLED_PIN;
        AlertDialog.Builder adb = createDontShowAgainDialog(true);
        adb.setMessage(R.string.guardian_disableScrambledPin);
        showGuardianDialog(adb);
    }

    /**
     * Warn the user to not disable screen protection.
     */
    public void securityScreenProtection() {
        mCurrentDialogName = DIALOG_DISABLE_SCREEN_PROTECTION;
        AlertDialog.Builder adb = createDontShowAgainDialog(true);
        adb.setMessage(R.string.guardian_disableScreenProtection);
        showGuardianDialog(adb);
    }

    /**
     * Warn the user about high On-Chain fees.
     * The user will be displayed a message which shows the amount of fee compared to
     * the transactions value.
     *
     * @param feeRate 0 = 0% ; 1 = 100% (equal transaction amount) ; >1 you pay more fees than you transact
     */
    public void securityHighOnChainFee(float feeRate) {
        mCurrentDialogName = DIALOG_HIGH_ONCHAIN_FEE;
        AlertDialog.Builder adb = createDontShowAgainDialog(true);
        String feeRateString = String.format("%.1f", feeRate * 100);
        adb.setMessage(mContext.getResources().getString(R.string.guardian_highOnChainFee, feeRateString));
        showGuardianDialog(adb);
    }

    /**
     * Warn the user if he tries to request some Bitcoin while his primary currency is a
     * fiat currency and the exchange rate data has come of age.
     *
     * @param age in seconds
     */
    public void securityOldExchangeRate(double age) {
        mCurrentDialogName = DIALOG_OLD_EXCHANGE_RATE;
        AlertDialog.Builder adb = createDontShowAgainDialog(true);
        String ageString = String.format("%.1f", age / 3600);
        adb.setMessage(mContext.getResources().getString(R.string.guardian_oldExchangeRate, ageString));
        showGuardianDialog(adb);
    }

    /**
     * Warn the user if he stores large amounts of Bitcoin in his wallet.
     */
    public void securityTooMuchMoney() {
        mCurrentDialogName = DIALOG_TOO_MUCH_MONEY;
        AlertDialog.Builder adb = createDontShowAgainDialog(false);
        adb.setMessage(R.string.guardian_tooMuchMoney);
        showGuardianDialog(adb);
    }

    /**
     * Warn the user if he is trying to connect to a remote server.
     */
    public void securityConnectToRemoteServer(String host) {
        mCurrentDialogName = DIALOG_REMOTE_CONNECT;
        AlertDialog.Builder adb = createDontShowAgainDialog(true);
        String message = mContext.getResources().getString(R.string.guardian_remoteConnect, host);
        adb.setMessage(message);
        showGuardianDialog(adb);
    }

    /**
     * Warn the user about accessing a transaction or address with a non tor block explorer.
     */
    public void privacyBlockExplorer() {
        mCurrentDialogName = DIALOG_BLOCK_EXPLORER;
        AlertDialog.Builder adb = createDontShowAgainDialog(true);
        adb.setMessage(R.string.guardian_blockExplorer);
        showGuardianDialog(adb);
    }


    /**
     * Create a dialog with a "do not show again" option that is already set up
     * except the message.
     * This helps keeping the dialog functions organized and simple.
     *
     * @param hasCancelOption wether it has a cancle option or not
     * @return returns a preconfigured AlertDialog.Builder which can be further configured later
     */
    private AlertDialog.Builder createDontShowAgainDialog(Boolean hasCancelOption) {
        AlertDialog.Builder adb = new AlertDialog.Builder(mContext);
        LayoutInflater adbInflater = LayoutInflater.from(mContext);
        View DialogLayout = adbInflater.inflate(R.layout.dialog_checkbox, null);
        mDontShowAgain = DialogLayout.findViewById(R.id.skip);
        View titleView = adbInflater.inflate(R.layout.guardian_title, null);
        adb.setView(DialogLayout);
        adb.setCustomTitle(titleView);
        adb.setPositiveButton(R.string.ok, (dialog, which) -> {

            if (mDontShowAgain.isChecked()) {
                PrefsUtil.edit().putBoolean(mCurrentDialogName, false).apply();
            }

            if (mListener != null) {
                // Execute interface callback on "OK"
                mListener.onGuardianConfirmed();
            }
        });
        if (hasCancelOption) {
            adb.setNegativeButton(R.string.cancel, (dialog, which) -> {
            });
        }
        return adb;
    }


    /**
     * Show the dialog or execute callback if it should not be shown.
     *
     * @param adb The AlertDialog.Builder which should be shown.
     */
    private void showGuardianDialog(AlertDialog.Builder adb) {

        if (PrefsUtil.getPrefs().getBoolean(mCurrentDialogName, true)) {
            Dialog dlg = adb.create();
            // Apply FLAG_SECURE to dialog to prevent screen recording
            if (PrefsUtil.preventScreenRecording()) {
                dlg.getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);
            }
            dlg.show();
        } else {
            mListener.onGuardianConfirmed();
        }
    }

    public interface OnGuardianConfirmedListener {
        void onGuardianConfirmed();
    }
}
