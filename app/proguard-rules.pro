# Keep Glance ActionCallback zero-arg constructors for RunCallbackAction
-keep class * implements androidx.glance.appwidget.action.ActionCallback {
    public <init>();
}
