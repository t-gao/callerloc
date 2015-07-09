callerloc
=========
号码归属地

Check out the 2nd generation (https://github.com/t-gao/callerloc2) which uses binary data instead of Sqlite, the size of the apk has been tremendously reduced from about 8MB to 300KB.

An Android app to show location info of incoming calls or outgoing calls in a floating window. Only works for numbers from mainland China.

The data is read from a db which is pre-created. Prior to Android 2.3, any compressed asset file with an uncompressed size of over 1 MB cannot be read from the APK. With this limitation, the db file is cut into several pieces which are combined back together to the one database file on the first run of the app. An index is also created to speed up the query of the db.
