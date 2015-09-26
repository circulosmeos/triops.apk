triops.apk:   
a simple Android tool for encryption/decryption of files.   

apk available here:   
[http://wp.me/p2FmmK-8T](http://wp.me/p2FmmK-8T)

![](https://circulosmeos.files.wordpress.com/2015/09/triops-apk1.png)

It uses [CHACHA20](http://en.wikipedia.org/wiki/Salsa20#ChaCha_variant) as algorithm for encryption/decryption and [KECCAK](http://en.wikipedia.org/wiki/SHA-3)-512 as hash algorithm.   

It is based on command-line tool triops, available [here](https://www.github.com/circulosmeos/triops). The C code is exactly the same, used via JNI, with just a GUI frontend for Android.   

Features:   

* Same content produces different encrypted outputs every time. This is attained with a random initialization vector (IV) stored within the encrypted file.
* Files are (by default) encrypted/decrypted on-the-fly, so content is overwritten. This is interesting from a security point of view, as no clear content is left on disk.
* When decrypting, if password is not the one used for encrypting, the process is aborted, so the file cannot be rendered unusable. This behaviour is achieved thanks to a password hint stored within the encrypted file.
* Mentioned hint used to check that the password for decryption is correct is *not* the same used to encrypt (obviously!). Separate hashes are used for both purposes, though both are derived via different ways from the password and IV, using some 500-1000 concatenated KECCAK hashes.
* Encrypted files are appended the extension .$#3 to filename, so they can be recognized.
* Password can be obtained from keyboard or from a file:
* Binary files can be used as passwords: for example jpg images, etc. Caution: do not lose this 'password' file and do not modify it!   
![](https://circulosmeos.files.wordpress.com/2015/09/triops-apk-file_as_password.png)
* Speed is extremely high: the app uses a native OS library and CHACHA20 is a very fast encryption algorithm: it is as fast as RC4.
* Code for the Android file browser is a slightly modified version of ingyesid‘s [simple-file-chooser](https://github.com/ingyesid/simple-file-chooser).
* Licensed as [GPL v3](http://www.gnu.org/licenses/gpl-3.0.html)

Known limitations:   

* Files greater than 2 GiB cannot be managed.
* Original file modification time is not maintained in Android as it is on cmdline tool, due to [some platform inherent problems](https://code.google.com/p/android/issues/detail?id=18624).
   
   
   
Please, refer to [triops](https://www.github.com/circulosmeos/triops) project for more details.
   
   

Note:   
* The sample encrypted file "gplv3.txt.$#3" can be decrypted with the password "triops!"   


