triops.apk
==========

A simple Android tool for encryption/decryption of files.   

Last version available and compiled is v1.5. Check [list of changes between versions](Changes.md).   

apk available [here](https://github.com/circulosmeos/triops.apk/releases/download/v1.5/triops.apk):   
[http://wp.me/p2FmmK-8T](http://wp.me/p2FmmK-8T)

![](https://circulosmeos.files.wordpress.com/2015/09/triops-apk1.png)

It uses [CHACHA20](http://en.wikipedia.org/wiki/Salsa20#ChaCha_variant) as algorithm for encryption/decryption and [KECCAK](http://en.wikipedia.org/wiki/SHA-3)-512 as hash algorithm.   

It is based on command-line tool triops, available [here](https://www.github.com/circulosmeos/triops). The C code is exactly the same, used via JNI, with just a GUI frontend for Android.   

Features
======== 

* Same content produces different encrypted outputs every time. This is attained with a random initialization vector (IV) stored within the encrypted file.
* Files are (by default) encrypted/decrypted on-the-fly, so content is overwritten. This is interesting from a security point of view, as no clear content is left on disk.
* When decrypting, if password is not the one used for encrypting, the process is aborted, so the file cannot be rendered unusable. This behaviour is achieved thanks to a password hint stored within the encrypted file.
* Mentioned hint used to check that the password for decryption is correct is *not* the same used to encrypt (obviously!). Separate hashes are used for both purposes, though both are derived via different ways from the password and IV, using some 500-1000 concatenated KECCAK hashes.
* Encrypted files are appended the extension .ooo to filename, so they can be recognized.
* Password can be obtained from keyboard or from a file:
* Binary files can be used as passwords: for example jpg images, etc. Caution: do not lose this 'password' file and do not modify it!   
![](https://circulosmeos.files.wordpress.com/2015/09/triops-apk-file_as_password.png)
* Files bigger than 2 GiB can be managed.
* Speed is extremely high: the app uses a native OS library and CHACHA20 is a very fast encryption algorithm: it is as fast as RC4.
* Code for the Android file browser is a slightly modified version of ingyesid‘s [simple-file-chooser](https://github.com/ingyesid/simple-file-chooser): see [circulosmeos/simple-file-chooser](https://github.com/circulosmeos/simple-file-chooser).
* Support for MultiWindow and drag-and-drop on Samsung's devices (file path text must be used, via drag-n-drop Samsung's MultiWindow button).
* Licensed as [GPL v3](http://www.gnu.org/licenses/gpl-3.0.html)

Known limitations
=================

* Original file modification time is not maintained in Android as it is on cmdline tool, due to [some platform inherent problems](https://code.google.com/p/android/issues/detail?id=18624).
   

Additional info
===============   
   
Please, refer to [triops](https://www.github.com/circulosmeos/triops) command-line project for more details.
      

Notes
===== 

* The sample encrypted file "gplv3.txt.ooo" can be decrypted with the password "triops!"   
   
* Files encrypted with version < 1.4 can be decrypted with greater versions, but in general files encrypted with version >=1.4 cannot be decrypted with previous versions. This is related to the change to cmdline triops's version >=9.0 : see [project notes here](https://github.com/circulosmeos/triops).   

* Version < 1.4 used the extension ".#$3" for encrypted files. It has been changed to ".ooo", though those files are correctly recognized and managed. See previous note.   


Known issues
============

Please, refer to [the issues page](https://github.com/circulosmeos/triops.apk/issues).	

* If you are using Android < 7 in a Samsung device, you may want to [install v1.4](https://github.com/circulosmeos/triops.apk/releases/download/v1.4/triops.apk), as it has Samsungs Multi-Windows support. This feature was removed in v1.5 as it is incompatible with Android N (7).


License
=======

Licensed as [GPL v3](http://www.gnu.org/licenses/gpl-3.0.en.html) or higher.   
