#http://bumptech.github.io/glide/doc/download-setup.html#proguard
#http://bumptech.github.io/glide/doc/download-setup.html#proguard

-keep public class * implements com.bumptech.glide.module.GlideModule
-keep class * extends com.bumptech.glide.module.AppGlideModule {
 <init>(...);
}
-keep public enum com.bumptech.glide.load.ImageHeaderParser$** {
  **[] $VALUES;
  public *;
}
-keep class com.bumptech.glide.load.data.ParcelFileDescriptorRewinder$InternalRewinder {
  *** rewind();
}

# for DexGuard only
# -keepresourcexmlelements manifest/application/meta-data@value=GlideModule