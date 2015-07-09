#Callerloc
号码归属地

Check out the 2nd generation (https://github.com/t-gao/callerloc2) which uses binary data instead of Sqlite, the size of the apk has been tremendously reduced from about 8MB to 300KB.

An Android app to show location info of incoming calls or outgoing calls in a floating window. Only works for numbers from mainland China.

The data is read from a db which is pre-created. Prior to Android 2.3, any compressed asset file with an uncompressed size of over 1 MB cannot be read from the APK. With this limitation, the db file is cut into several pieces which are combined back together to the one database file on the first run of the app. An index is also created to speed up the query of the db.

#License
Copyright 2015 Tony Gao

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
