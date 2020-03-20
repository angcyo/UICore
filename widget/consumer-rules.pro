
# com.angcyo.widget.recycler.RecyclerExKt.getLastVelocity 获取RV的Fling的速率
-keepclassmembers class androidx.recyclerview.widget.RecyclerView {
   androidx.recyclerview.widget.RecyclerView$ViewFlinger mViewFlinger;
   #<fields>;
}
-keepclassmembers class androidx.recyclerview.widget.RecyclerView$ViewFlinger {
   android.widget.OverScroller mOverScroller;
   #<fields>;
}
-keepclassmembers class androidx.core.widget.NestedScrollView {
   android.widget.OverScroller mScroller;
   #<fields>;
}
# end...

# com.angcyo.widget.base.TextViewExKt.clearListeners 系统的类, 全部不会被混淆
# -keepclassmembers class android.widget.TextView {
#   java.util.ArrayList mListeners;
# }
# end...