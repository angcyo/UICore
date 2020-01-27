#com.liulishuo.okdownload.core.Util.createDefaultDatabase
-keep class com.liulishuo.okdownload.core.breakpoint.BreakpointStoreOnSQLite

#com.liulishuo.okdownload.core.Util.createDefaultConnectionFactory
-keep class com.liulishuo.okdownload.core.connection.DownloadOkHttp3Connection$Factory

# okhttp https://github.com/square/okhttp/#proguard
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn javax.annotation.**
-dontwarn org.conscrypt.**
# A resource is loaded with a relative path so the package of this class must be preserved.
-keepnames class okhttp3.internal.publicsuffix.PublicSuffixDatabase

# okdownload:okhttp
-keepnames class com.liulishuo.okdownload.core.connection.DownloadOkHttp3Connection

# okdownload:sqlite
-keep class com.liulishuo.okdownload.core.breakpoint.BreakpointStoreOnSQLite {
        public com.liulishuo.okdownload.core.breakpoint.DownloadStore createRemitSelf();
        public com.liulishuo.okdownload.core.breakpoint.BreakpointStoreOnSQLite(android.content.Context);
}

# Findbugs
-dontwarn edu.umd.cs.findbugs.annotations.SuppressFBWarnings