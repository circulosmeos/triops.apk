package com.example.triops;

import android.content.ClipData;
import android.content.ClipDescription;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.ActionBarActivity;
import android.text.InputType;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.DragEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.orleonsoft.android.simplefilechooser.Constants;
import com.orleonsoft.android.simplefilechooser.ui.FileChooserActivity;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends ActionBarActivity {

	private final String strBtnDecrypt="decrypt";
	private final String strBtnEncrypt="crypt !";
	private final String strBtnProceed="proceed";
	private final int colorDecryptBackground=0xFF62FF73;
	private final int colorEncryptBackground=0xFFFF6B5A;
	private final int colorProceedBackground=0xFF8080FF;

	private final String strTriopsGenericExtension="ooo"; // triops v>=9.0
	private final String strTriopsExtensionMatch=".+\\.(\\$#3|ooo)$"; 	  // triops v<9.0 && triops v>=9.0
	private String strParameterForEncrypting="3"; // triops' default encrypting scheme 

	private final int REQUEST_CODE_GET_PASSWORD_FILE = 1;
	private final int REQUEST_CODE_GET_FILE_TO_OVERWRITE = 2;

	/****************************
	 	usgin logs on Activity
	 ****************************/
	private String strLogs;
	TextView tvwLog;
	ScrollView svwLog;

	private enum FILE_OPERATION { OPERATION_CRYPT, OPERATION_DECRYPT }

	private boolean bEdtPassword = true;
	private boolean bPasswordMethodFile = false;
	private ArrayList<String> strExternalFile = null;
	private String MULTIPLE_FILES_SELECTED = "Multiple files selected";
	private String strExternalPasswordFile;

    /* this is used to load the 'name' library on application
     * startup. The library has already been unpacked into
     * /data/data/com.example.name/lib/libname.so at
     * installation time by the package manager.
     */
    static {
        System.loadLibrary("com.example.triops"); // triops
    }

	// predefinition of JNI (external) method
	//public native int triops(int argc, String[] argv);
	public native int triops(String[] argv); // triops

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
			public void run() {
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
		ArrayList<String> filesOperated = new ArrayList<String>();
		boolean bError = false;
		int iCommandReturnedValue;
		int iErrors = 0;

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
			/*if (!strPassword.matches("^[\\w\\.\\[\\]\\{\\}\\-!#$%&/()=?^*\\{\\},;:@+]+$")) {
				Toast.makeText(
						this,
						"Password contains invalid characters!",
						Toast.LENGTH_LONG
				).show();
				return;
			}*/ /*else {
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
		// 3. check that there're file(s) selected:
		// .............................................
		if ( strExternalFile == null ||
				! strExternalFile.get(0).matches("^/.+$") ) {
			Toast.makeText(
					this,
					"Sorry, there's no valid file selected.",
					Toast.LENGTH_LONG
			).show();
			return;
		}

		// now begins the operation loop over files:
		for (String file: strExternalFile) {

			// .............................................
			// 4. infer operation from file extension:
			// .............................................
			if ( file.matches( strTriopsExtensionMatch ) ) {
				operation = FILE_OPERATION.OPERATION_DECRYPT;
			} else {
				operation = FILE_OPERATION.OPERATION_CRYPT;
			}

			// .............................................
			// 5. construct the command string:
			// .............................................
			if ( operation == FILE_OPERATION.OPERATION_CRYPT ) {
				strCommand = new String[7];
				strCommand[5] = "-e";
				strCommand[6] = strParameterForEncrypting;
			} else {
				strCommand = new String[5];
			}
			// we're gonna fake a command line operation with argc + *argv[]:
			strCommand[0]="triops"; // first parameter is "executable" name, so, here: any value.
			if ( ! bPasswordMethodFile ) {
				strCommand[3] = "-p";
				strCommand[4] = strPassword;
			} else {
				strCommand[3] = "-P";
				strCommand[4] = strPassword;
			}
			strCommand[1] = "-i";
			strCommand[2] = file;

			/*
			strLogs +=strCommand[0]+" "+strCommand[1]+" "+strCommand[2]+" "+strCommand[3]+" "+strCommand[4];
			if (operation == FILE_OPERATION.OPERATION_CRYPT) strLogs+=" "+strCommand[5]+" "+strCommand[6];
			showLogs();
			*/

			// .............................................
			// 6. perform command & check output
			// .............................................
			// TODO: run in a Runnable the triops(strCommand)
			iCommandReturnedValue = triops(strCommand);
			if ( iCommandReturnedValue != 0) { // triops
				// Error !!!
				// go on, but marks failure
				bError = true;
				iErrors++;
				if (operation == FILE_OPERATION.OPERATION_CRYPT ) {
					strLogs += "\n\nERROR encrypting:\n";
				} else {
					strLogs += "\n\nERROR decrypting:\n";
				}
				strLogs += file + "\n\n";
				showLogs();
			} else {
				// Correct
				// go on:
				strLogs += "\nCorrect ";
				if (operation == FILE_OPERATION.OPERATION_CRYPT ) {
					strLogs += "encryption";
				} else {
					strLogs += "decryption";
				}
				strLogs += " on: " + extractFilename(file) + "\n";

				if (file.matches( strTriopsExtensionMatch )) {
					filesOperated.add(file.substring(0, file.length() - 4) );
				} else {
					filesOperated.add(file + "." + strTriopsGenericExtension );
				}

				// special case management:
				// the previously encrypted file was previously and erroneously selected as password file:
				// in this case, erase that selection not to allow to pass an inexistent file path.
				if ( strExternalPasswordFile != null &&
						strExternalPasswordFile.length() > 0 &&
						strExternalPasswordFile.equals(file) ) {
					strExternalPasswordFile="";
					((TextView)findViewById(R.id.tvwPasswordFile)).setText("");
				}

			}

		}

		// .............................................
		// 7. final operations for reversible behaviour
		// .............................................
		// change selected file name, so the next operation can be directly the reverse
		// of the previous one, without the needing for a new file selection.
		if ( !bError ) {
			// change selected files for their inversed extensions,
			// so the inverse op is possible wuth just a click
			strExternalFile = filesOperated;
		} else {
			// clear file selection:
			if ( strExternalFile.size()==1 || (iErrors == strExternalFile.size()) ) {
				// but if there's only one file selected or {multiple files and all of them failed}
				// then do not clear file selections, so another password try be possible
			} else {
				strExternalFile = null;
				((TextView) findViewById(R.id.tvwInfo)).setText(R.string.info_choose_file);
				// to default state (crypt):
				((TextView) findViewById(R.id.btnAction)).setText(strBtnEncrypt);
				((TextView) findViewById(R.id.btnAction)).getBackground().setColorFilter(colorEncryptBackground, PorterDuff.Mode.MULTIPLY);
			}
		}
		// in case of just one file selected, update GUI for reversible op:
		if ( !bError &&
				strExternalFile.size() == 1 ) {
			// be careful: strExternalFile has already been reversed, so op is the opposite!
			if (strExternalFile.get(0).matches( strTriopsExtensionMatch )) {
				((TextView) findViewById(R.id.btnAction)).setText(strBtnDecrypt);
				((TextView) findViewById(R.id.btnAction)).getBackground().setColorFilter(colorDecryptBackground, PorterDuff.Mode.MULTIPLY);
				// and show it:
				strLogs += "next action: DECRYPT\non:\n" + extractFilename(strExternalFile.get(0)) + "\n\n";
			} else {
				((TextView) findViewById(R.id.btnAction)).setText(strBtnEncrypt);
				((TextView) findViewById(R.id.btnAction)).getBackground().setColorFilter(colorEncryptBackground, PorterDuff.Mode.MULTIPLY);
				// and show it:
				strLogs += "next action: CRYPT\non:\n" + extractFilename(strExternalFile.get(0)) + "\n\n";
			}
			// and show it:
			((TextView) findViewById(R.id.tvwInfo)).setText(strExternalFile.get(0));
		}
		// in case more than one file were selected, update GUI for reversible op if possible:
		if ( !bError &&
				strExternalFile.size() > 1 ) {
			int iCount = 0;
			for (String file: strExternalFile) {
				if ( file.matches( strTriopsExtensionMatch ) ) {
					iCount++;
				}
			}
			if ( iCount == strExternalFile.size() ) {
				((TextView) findViewById(R.id.btnAction)).setText(strBtnDecrypt);
				((TextView) findViewById(R.id.btnAction)).getBackground().setColorFilter(colorDecryptBackground, PorterDuff.Mode.MULTIPLY);
				// and show it:
				strLogs += "\nnext action: DECRYPT\n\n";
			} else if (iCount == 0) {
				((TextView) findViewById(R.id.btnAction)).setText(strBtnEncrypt);
				((TextView) findViewById(R.id.btnAction)).getBackground().setColorFilter(colorEncryptBackground, PorterDuff.Mode.MULTIPLY);
				// and show it:
				strLogs += "\nnext action: CRYPT\n\n";
			}
		}

		showLogs();

	}


	/**************************************
	 	file browser integration with fork of
	 https://github.com/ingyesid/simple-file-chooser
	**************************************/

	// select file to use as password
	public void selectPasswordFile(View v){

		Intent fileExploreIntent = new Intent(MainActivity.this, FileChooserActivity.class);

		if (strExternalPasswordFile != null &&
				strExternalPasswordFile.matches("^(.*/)[^/]+") ) {
			fileExploreIntent.putExtra(
					Constants.KEY_INITIAL_DIRECTORY,
					extractPath(strExternalPasswordFile)
			);
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
			strExternalFile.size()>0 &&
			strExternalFile.get(0).matches("^(.*/)[^/]+") ) {
				fileExploreIntent.putExtra(
						Constants.KEY_INITIAL_DIRECTORY,
						extractPath(strExternalFile.get(0))
				);
		}

		// implements checkable items
		fileExploreIntent.putExtra(
				Constants.KEY_SHOW_CHECKBOXES_FOR_FILES,
				Constants.KEY_SHOW_CHECKBOXES_FOR_FILES
		);

		startActivityForResult(
				fileExploreIntent,
				REQUEST_CODE_GET_FILE_TO_OVERWRITE
		);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	    if (requestCode == REQUEST_CODE_GET_FILE_TO_OVERWRITE) {
			if(resultCode == RESULT_OK) {
				// implements checkable items
				ArrayList<String> list_of_selected_files = null;
				try {
					list_of_selected_files =
							(ArrayList<String>) data.getStringArrayListExtra(Constants.KEY_FILE_SELECTED);
				} catch (NullPointerException e) {
					// This code should never be reached:
					// there hasn't been any file selected,
					// so exit now to maintain previous selections
					return;
				}
				// set file name:
				if (list_of_selected_files != null) {
					strExternalFile = list_of_selected_files;
				} else {
					// This code should never be reached:
					// there hasn't been any file selected,
					// so exit now to maintain previous selections
					return;
				}

				// update Activity:
				// .............................................
				// 1. show selected filename(s)
				// .............................................
				if (strExternalFile.size() == 1) {
					((TextView) findViewById(R.id.tvwInfo)).setText(strExternalFile.get(0));
					strLogs += strExternalFile.get(0) + "\n";
				} else {
					((TextView) findViewById(R.id.tvwInfo)).setText(MULTIPLE_FILES_SELECTED);
					// first, extract and show path
					strLogs += "\nIn folder\n\t" + extractPath(strExternalFile.get(0)) + "\nyou've selected:\n";
					for (String file : strExternalFile) {
						strLogs += "\t" + extractFilename(file) + "\n";
					}
				}

				// .............................................
				// 2. infer operation from file extension:
				// .............................................
				if (strExternalFile.size() == 1) {
					if (strExternalFile.get(0).matches( strTriopsExtensionMatch )) {
						((TextView) findViewById(R.id.btnAction)).setText(strBtnDecrypt);
						((TextView) findViewById(R.id.btnAction)).getBackground().setColorFilter(colorDecryptBackground, PorterDuff.Mode.MULTIPLY);
						// and show it:
						strLogs += "Action: DECRYPT\n";
					} else {
						((TextView) findViewById(R.id.btnAction)).setText(strBtnEncrypt);
						((TextView) findViewById(R.id.btnAction)).getBackground().setColorFilter(colorEncryptBackground, PorterDuff.Mode.MULTIPLY);
						// and show it:
						strLogs += "Action: CRYPT\n";
					}
				} else {
					int iCrypt = 0, iDecrypt = 0;
					for (String file : strExternalFile) {
						if (file.matches( strTriopsExtensionMatch )) {
							iDecrypt++;
						} else {
							iCrypt++;
						}
					}
					if (iCrypt==0 && iDecrypt!=0) {
						((TextView) findViewById(R.id.btnAction)).setText(strBtnDecrypt);
						((TextView) findViewById(R.id.btnAction)).getBackground().setColorFilter(colorDecryptBackground, PorterDuff.Mode.MULTIPLY);
						strLogs += "\nAction: " + Integer.toString(iDecrypt) + " DECRYPTs\n";
					}
					else if (iDecrypt==0 && iCrypt!=0) {
						((TextView) findViewById(R.id.btnAction)).setText(strBtnEncrypt);
						((TextView) findViewById(R.id.btnAction)).getBackground().setColorFilter(colorEncryptBackground, PorterDuff.Mode.MULTIPLY);
						strLogs += "\nAction: " + Integer.toString(iCrypt) + " CRYPTs\n";
					}
					else {
						strLogs += "\nActions: " + Integer.toString(iCrypt) + " CRYPTs and " + Integer.toString(iDecrypt) + " DECRYPTs\n";
						((TextView) findViewById(R.id.btnAction)).setText(strBtnProceed);
						((TextView) findViewById(R.id.btnAction)).getBackground().setColorFilter(colorProceedBackground, PorterDuff.Mode.MULTIPLY);
					}
					showLogs();
				}
				// special case check:
				special_case_warning();
				// show this to proceed:
				strLogs += "\n\nClick on button to proceed...\n";
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
				// set file name:
				strExternalPasswordFile = data.getStringArrayListExtra(
						Constants.KEY_FILE_SELECTED).get(0);
				/*Toast.makeText(
					this,
					"Received file name from file browser:"+strExternalPasswordFile,
					Toast.LENGTH_LONG
				).show();*/

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
	/**************************************
	 	END of file browser integration with
	 https://github.com/ingyesid/simple-file-chooser
	 **************************************/


	// returns just the path to a file, given its complete path string
	private String extractPath(String strFile) {
		Matcher mtchPath=Pattern.compile("^(.*/)[^/]+").matcher(strFile);
		mtchPath.find();
		if ( ! "".matches( mtchPath.group(1) ) ) {
			return mtchPath.group(1);
		} else {
			return "";
		}
	}


	// returns just the filename, given its complete path string
	private String extractFilename(String strFile) {
		Matcher mtchPath=Pattern.compile("^.*/([^/]+)").matcher(strFile);
		mtchPath.find();
		if ( ! "".matches( mtchPath.group(1) ) ) {
			return mtchPath.group(1);
		} else {
			return "";
		}
	}

	// Warning if crypting a file using itself as password
	private void special_case_warning() {
		// special case warning:
		if ( strExternalFile != null &&
				strExternalFile.size()>0 &&
				bPasswordMethodFile &&
				strExternalPasswordFile != null &&
				strExternalPasswordFile.length() > 0 ) {
			for (String file: strExternalFile) {
				if ( file.length() > 0 &&
						!file.matches( strTriopsExtensionMatch ) &&
						file.equals(strExternalPasswordFile)
						) {
					strLogs += "\n\nWARNING:\n     WARNING:\n          WARNING:\n" +
							"Crypting this file with itself as password will render the encrypted data IRRECOVERABLE:\n\t" +
							file +
							"\nAs this is licit for data wiping, it's up to you to continue.\n";
				}
			}
		}
	}

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
