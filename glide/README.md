# glide
2020-01-20

https://muyangmin.github.io/glide-docs-cn/

https://github.com/bumptech/glide

https://muyangmin.github.io/glide-docs-cn/doc/download-setup.html

Status
Version 4 is now released and stable. Updates are released periodically with new features and bug fixes.

Comments/bugs/questions/pull requests are always welcome! Please read CONTRIBUTING.md on how to report issues.

Compatibility
Minimum Android SDK: Glide v4 requires a minimum API level of 14.
Compile Android SDK: Glide v4 requires you to compile against API 26 or later.
If you need to support older versions of Android, consider staying on Glide v3, which works on API 10, but is not actively maintained.

OkHttp 3.x: There is an optional dependency available called okhttp3-integration, see the docs page.
Volley: There is an optional dependency available called volley-integration, see the docs page.
Round Pictures: CircleImageView/CircularImageView/RoundedImageView are known to have issues with TransitionDrawable (.crossFade() with .thumbnail() or .placeholder()) and animated GIFs, use a BitmapTransformation (.circleCrop() will be available in v4) or .dontAnimate() to fix the issue.
Huge Images (maps, comic strips): Glide can load huge images by downsampling them, but does not support zooming and panning ImageViews as they require special resource optimizations (such as tiling) to work without OutOfMemoryErrors.