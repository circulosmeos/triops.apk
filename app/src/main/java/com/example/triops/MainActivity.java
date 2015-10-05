package com.example.triops;

import android.graphics.PorterDuff;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.ActionBarActivity;
import android.content.Intent;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.EditText;
import android.widget.Toast;
import android.text.InputType;

import com.orleonsoft.android.simplefilechooser.Constants;
import com.orleonsoft.android.simplefilechooser.ui.FileChooserActivity;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends ActionBarActivity {

	private final String strBtnDecrypt="decrypt";
	private final String strBtnEncrypt="crypt !";
	private final int colorDecryptBackground=0xFF62FF73;
	private final int colorEncryptBackground=0xFFFF6B5A;

	private final String strExtension="$#3";
	private final String strParameterForEncrypting="3";

	private final int REQUEST_CODE_GET_PASSWORD_FILE = 1;
	private final int REQUEST_CODE_GET_FILE_TO_OVERWRITE = 2;

	/****************************
	 	usgin logs on Activity
	 ****************************/
	private String strLogs;
	TextView tvwLog;
	ScrollView svwLog;

	private enum FILE_OPERATION { OPERATION_CRYPT, OPERATION_DECRYPT }
	
	private boolean bEdtPassword=true;
	private boolean bPasswordMethodFile=false;
	private String strExternalFile;
	private String strExternalPasswordFile;

    /* this is used to load the 'name' library on application
     * startup. The library has already been unpacked into
     * /data/data/com.example.name/lib/libname.so at
     * installation time by the package manager.
     */
    static {
        System.loadLibrary("com.example.triops");
    }

	// predefinition of JNI (external) method
	//public native int triops(int argc, String[] argv);
	public native int triops(String[] argv);
	
	final FragmentActivity activityForButton = this;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		tvwLog = (TextView)findViewById(R.id.tvw_log_text);
    	svwLog = (ScrollView)findViewById(R.id.svw_log_scroller);
    	strLogs = getResources().getString(R.string.initiallogtext);

		// set color for btnAction by code: this way the button do not loose its "button" feeling:
		((TextView)findViewById(R.id.btnAction)).getBackground().setColorFilter(colorEncryptBackground, PorterDuff.Mode.MULTIPLY);

		// set icon in toolbar:
		getSupportActionBar().setDisplayShowHomeEnabled(true);
		getSupportActionBar().setIcon(R.drawable.ic_launcher);

		// show links in tvwLog
		tvwLog.setMovementMethod(LinkMovementMethod.getInstance());
		// this will last just until next tvwLog modification ! Enough.
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		if (id == R.id.submenu_exit) {
			onExitItemClick();
		}
		return super.onOptionsItemSelected(item);
	}
	
	private void onExitItemClick(){

		// delete any trace of use in this session:
		final String strOverwrite="012345678901234567890123456789";
		((EditText)findViewById(R.id.edtPassword)).setText(strOverwrite);
		((TextView)findViewById(R.id.tvwInfo)).setText(strOverwrite);
		((TextView)findViewById(R.id.tvwPasswordFile)).setText(strOverwrite);
		strLogs ="";
		showLogs();
		findViewById(R.id.edtPassword).setVisibility(View.INVISIBLE);
		findViewById(R.id.tvwPassword).setVisibility(View.INVISIBLE);
		findViewById(R.id.chkPassword).setVisibility(View.INVISIBLE);
		findViewById(R.id.btnChoosePasswordFile).setVisibility(View.INVISIBLE);
		findViewById(R.id.tvwInfo).setVisibility(View.INVISIBLE);
		findViewById(R.id.tvwPasswordFile).setVisibility(View.INVISIBLE);
		findViewById(R.id.tglbtn_password_method).setVisibility(View.INVISIBLE);
		findViewById(R.id.btnAction).setVisibility(View.INVISIBLE);
		// and just not to leave anything:
		findViewById(R.id.btnChooseFile).setVisibility(View.INVISIBLE);
		findViewById(R.id.tvwInfoHeader).setVisibility(View.INVISIBLE);

		// in order to overwrite the screen capture of the task with
		// this empty state, let's wait some time before closing:
		Handler h = new Handler();
		h.postDelayed(new Runnable() {
			@Override
			public void run(){
				triops_exit();
			}
		}, 555);
	}

	private void triops_exit() {
		this.finish();
		android.os.Process.killProcess(android.os.Process.myPid());
	}

	// toggle show/hide password
	public void edtPassword_toggle(View v) {
		if ( ! bEdtPassword ) {
			bEdtPassword=true;
			((EditText)findViewById(R.id.edtPassword)).setInputType(
					(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD));
		} else {
			bEdtPassword=false;
			((EditText)findViewById(R.id.edtPassword)).setInputType(
					(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD));
		}
	}

	// toggle password method: text password / file password
	public void tglbtn_password_method_toggle(View v){

		if ( ! bPasswordMethodFile ) {
			// select file to use as password
			bPasswordMethodFile=true;
			((EditText)findViewById(R.id.edtPassword)).setVisibility(View.INVISIBLE);
			((TextView)findViewById(R.id.tvwPassword)).setVisibility(View.INVISIBLE);
			((CheckBox)findViewById(R.id.chkPassword)).setVisibility(View.INVISIBLE);
			((Button)findViewById(R.id.btnChoosePasswordFile)).setVisibility(View.VISIBLE);
			((TextView)findViewById(R.id.tvwPasswordFile)).setVisibility(View.VISIBLE);
		} else {
			// insert text password
			bPasswordMethodFile=false;
			((EditText)findViewById(R.id.edtPassword)).setVisibility(View.VISIBLE);
			((TextView)findViewById(R.id.tvwPassword)).setVisibility(View.VISIBLE);
			((CheckBox)findViewById(R.id.chkPassword)).setVisibility(View.VISIBLE);
			((Button)findViewById(R.id.btnChoosePasswordFile)).setVisibility(View.INVISIBLE);
			((TextView)findViewById(R.id.tvwPasswordFile)).setVisibility(View.INVISIBLE);
		}

		// special case check
		special_case_warning();

	}

	// Start the crypting/decrypting!
	public void crypt_operation(View v) {
		
		/*
		enum FILE_OPERATION { OPERATION_CRYPT, OPERATION_DECRYPT }
		 */
		
		FILE_OPERATION operation;
		String strPassword;
		String[] strCommand;
		
		// .............................................
		// 0. extract password value
		// .............................................
		if ( bPasswordMethodFile ) {
			strPassword = strExternalPasswordFile;
		} else {
			strPassword = ((EditText) findViewById(R.id.edtPassword)).getText().toString();
		}
		
		// .............................................
		// 1. check that there's a password, and that it's acceptable:
		// .............................................
		if ( ! bPasswordMethodFile ) {
			if (!strPassword.matches("^[\\w\\.\\[\\]\\{\\}\\-!#$%&/()=?^*\\{\\},;:@+]+$")) {
				Toast.makeText(
						this,
						"Password contains invalid characters!",
						Toast.LENGTH_LONG
				).show();
				return;
			} /*else {
				if ( strPassword.matches("^__$") ) {
					Toast.makeText(
							this,
							"Sorry, that password is restricted from use for compatibility reasons.",
							Toast.LENGTH_LONG
						).show();
					return;
				}
			}*/
		}
		if ( bPasswordMethodFile &&
				( strExternalPasswordFile == null ||
				  strExternalPasswordFile.length() == 0) ) {
			Toast.makeText(
					this,
					"No file to be used as password selected.",
					Toast.LENGTH_LONG
			).show();
			return;
		}
		if (strPassword.length() > 255) {
			String strText;
			if ( bPasswordMethodFile ) {
				strText="path to password file is too long";
			} else {
				strText="password text is too long";
			}
			Toast.makeText(
					this,
					"Sorry, " + strText + " (>255 chars).",
					Toast.LENGTH_LONG
			).show();
			return;
		}

		// .............................................
		// 2. infer operation from file extension:
		// .............................................
		if ( strExternalFile == null ||
			 ! strExternalFile.matches("^/.+$") ) {
			Toast.makeText(
                    this, 
                    "Sorry, there's no valid file selected.", 
                    Toast.LENGTH_LONG
                ).show();
			return;							
		}
		if ( strExternalFile.matches(".+\\.\\" + strExtension + "$") ) {
			operation=FILE_OPERATION.OPERATION_DECRYPT;
		} else {
			operation=FILE_OPERATION.OPERATION_CRYPT;
		}
		
		// .............................................
		// 3. ok, perform the intended operation:
		// .............................................
		if ( operation == FILE_OPERATION.OPERATION_CRYPT ) {
			strCommand = new String[5];
			strCommand[4]=strParameterForEncrypting;
		} else {
			strCommand = new String[4];
		}
		// we're gonna fake a command line operation with argc + *argv[]:
		strCommand[0]="triops"; // first parameter is "executable" name, so, here: any value.
		if ( ! bPasswordMethodFile )
			strCommand[1]="_" + strPassword + "_";
		else
			strCommand[1]=strPassword;
		strCommand[2]=strExternalFile;
		strCommand[3]="=";

		/*
		strLogs +=strCommand[0]+" "+strCommand[1]+" "+strCommand[2]+" "+strCommand[3];
		if (operation == FILE_OPERATION.OPERATION_CRYPT) strLogs+=" "+strCommand[4];
		showLogs();
		*/

		//if ( triops(argc, strCommand) == 0) {
		if ( triops(strCommand) == 1) {
			if (operation == FILE_OPERATION.OPERATION_CRYPT ) {
				strLogs += "\n\nERROR encrypting:\n";
			} else {
				strLogs += "\n\nERROR decrypting:\n";
			}
			strLogs += strExternalFile + "\n\n";
    		showLogs();
		} else {
			// Correct
			strLogs += "\n\nCorrect ";
			if (operation == FILE_OPERATION.OPERATION_CRYPT ) {
				strLogs += "encryption";
			} else {
				strLogs += "decryption";
			}
			strLogs += " on:\n" + strExternalFile + "\n\n";

			// special case management:
			// the previously encrypted file was previously and erroneously selected as password file:
			// in this case, erase that selection not to allow to pass an inexistent file path.
			if ( strExternalPasswordFile != null &&
					strExternalPasswordFile.length() > 0 &&
					strExternalPasswordFile.equals(strExternalFile) ) {
				strExternalPasswordFile="";
				((TextView)findViewById(R.id.tvwPasswordFile)).setText("");
			}

			// change selected file name, so the next operation can be directly the reverse
			// of the previous one, without needing for a new file selection.
			if ( strExternalFile.matches(".+\\.\\" + strExtension + "$") ) {
				strExternalFile=strExternalFile.substring(0, strExternalFile.length()-4);
				((TextView)findViewById(R.id.btnAction)).setText(strBtnEncrypt);
				((TextView)findViewById(R.id.btnAction)).getBackground().setColorFilter(colorEncryptBackground, PorterDuff.Mode.MULTIPLY);
				// and show it:
				strLogs += "next action: CRYPT\non:\n" + strExternalFile + "\n\n";
			} else {
				strExternalFile+="." + strExtension;
				((TextView)findViewById(R.id.btnAction)).setText(strBtnDecrypt);
				((TextView)findViewById(R.id.btnAction)).getBackground().setColorFilter(colorDecryptBackground, PorterDuff.Mode.MULTIPLY);
				// and show it:
				strLogs += "next action: DECRYPT\non:\n" + strExternalFile + "\n\n";
			}
			// and show it:
			((TextView)findViewById(R.id.tvwInfo)).setText(strExternalFile);
			showLogs();

		}

	}


	/**************************************
	 	file browser integration with
	 https://github.com/ingyesid/simple-file-chooser
	**************************************/

	// select file to use as password
	public void selectPasswordFile(View v){

		Intent fileExploreIntent = new Intent(MainActivity.this, FileChooserActivity.class);

		if (strExternalPasswordFile != null &&
				strExternalPasswordFile.matches("^(.*/)[^/]+") ) {
			Matcher mtchPath=((Pattern)Pattern.compile("^(.*/)[^/]+")).matcher(strExternalPasswordFile);
			mtchPath.find();
			if ( ! "".matches( mtchPath.group(1) ) ) {
				fileExploreIntent.putExtra (
						Constants.KEY_INITIAL_DIRECTORY,
						mtchPath.group(1)
				);
			}
		}
		startActivityForResult(
				fileExploreIntent,
				REQUEST_CODE_GET_PASSWORD_FILE
		);
	}

	// select file to encrypt/decrypt
	public void selectFile(View v){

		   Intent fileExploreIntent = new Intent(MainActivity.this, FileChooserActivity.class);

			if (strExternalFile != null &&
				strExternalFile.matches("^(.*/)[^/]+") ) {
					Matcher mtchPath=((Pattern)Pattern.compile("^(.*/)[^/]+")).matcher(strExternalFile);
					mtchPath.find();
					if ( ! "".matches( mtchPath.group(1) ) ) {
						fileExploreIntent.putExtra(
								Constants.KEY_INITIAL_DIRECTORY,
								mtchPath.group(1)
						);
					}
			}
			startActivityForResult(
					fileExploreIntent,
					REQUEST_CODE_GET_FILE_TO_OVERWRITE
			);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	    if (requestCode == REQUEST_CODE_GET_FILE_TO_OVERWRITE) {
			if(resultCode == RESULT_OK) {
				String newFile = data.getStringExtra(
						Constants.KEY_FILE_SELECTED);
				/*Toast.makeText(
					this,
					"Received file name from file browser:"+newFile,
					Toast.LENGTH_LONG
				).show();*/
				// set file name:
				strExternalFile = newFile;

				// update Activity:
				// .............................................
				// 1. show selected filename
				// .............................................
				if (!strExternalFile.equals("")) {
					((TextView)findViewById(R.id.tvwInfo)).setText(strExternalFile);
				}

				// .............................................
				// 2. infer operation from file extension:
				// .............................................
				if ( strExternalFile.matches(".+\\.\\" + strExtension + "$") ) {
					((TextView)findViewById(R.id.btnAction)).setText(strBtnDecrypt);
					((TextView)findViewById(R.id.btnAction)).getBackground().setColorFilter(colorDecryptBackground, PorterDuff.Mode.MULTIPLY);
					// and show it:
					strLogs += "Action: DECRYPT\n";
					showLogs();
				} else {
					((TextView)findViewById(R.id.btnAction)).setText(strBtnEncrypt);
					((TextView)findViewById(R.id.btnAction)).getBackground().setColorFilter(colorEncryptBackground, PorterDuff.Mode.MULTIPLY);
					// and show it:
					strLogs += "Action: CRYPT\n";
					showLogs();
				}
				// special case check:
				special_case_warning();
				// and show it:
				strLogs += strExternalFile + "\n\nClick on button to proceed...";
				showLogs();

			} else {//if(resultCode == this.RESULT_OK) {
				Toast.makeText(
					this,
					"Received NO result from file browser",
					Toast.LENGTH_LONG)
				.show();
			}//END } else {//if(resultCode == this.RESULT_OK) {
		}
		if (requestCode == REQUEST_CODE_GET_PASSWORD_FILE) {
			if(resultCode == RESULT_OK) {
				String newFile = data.getStringExtra(
						Constants.KEY_FILE_SELECTED);
				/*Toast.makeText(
					this,
					"Received file name from file browser:"+newFile,
					Toast.LENGTH_LONG
				).show();*/
				// set file name:
				strExternalPasswordFile = newFile;

				// update Activity:
				if (!strExternalPasswordFile.equals("")) {
					((TextView)findViewById(R.id.tvwPasswordFile)).setText(strExternalPasswordFile);
				}

				// special case check:
				special_case_warning();

				// and show it:
				strLogs += "\nUsing this file as password:\n" + strExternalPasswordFile + "\n";
				showLogs();

			} else {//if(resultCode == this.RESULT_OK) {
				Toast.makeText(
						this,
						"Received NO result from file browser",
						Toast.LENGTH_LONG)
						.show();
			}//END } else {//if(resultCode == this.RESULT_OK) {
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	private void special_case_warning() {
		// special case warning:
		if ( strExternalFile != null &&
				strExternalPasswordFile != null &&
				strExternalFile.length() > 0 &&
				strExternalPasswordFile.length() > 0 &&
				((TextView)findViewById(R.id.btnAction)).getText().equals(strBtnEncrypt) ) {
			if ( strExternalFile.equals(strExternalPasswordFile) && bPasswordMethodFile ) {
				strLogs += "\n\nWARNING:\n     WARNING:\n          WARNING:\n" +
						"Crypting a file with itself as password will render the encrypted data IRRECOVERABLE.\n" +
						"As this is licit for data wiping, it's up to you to continue.\n";
			}
		}
	}
	/**************************************
	 	END of file browser integration with
	 https://github.com/ingyesid/simple-file-chooser
	 **************************************/


	/****************************
	 	usgin logs on Activity
	 ****************************/
	public void showLogs(){

		tvwLog.setText(strLogs);

    	svwLog.post(new Runnable() {
			public void run() {
				svwLog.fullScroll(View.FOCUS_DOWN);
			}
		});
	}


}
