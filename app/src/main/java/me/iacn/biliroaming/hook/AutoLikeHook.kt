package me.iacn.biliroaming.hook

import android.view.View
import me.iacn.biliroaming.BiliBiliPackage.Companion.instance
import me.iacn.biliroaming.utils.*

class AutoLikeHook(classLoader: ClassLoader) : BaseHook(classLoader) {
    private val likedVideos = HashSet<Long>()

    override fun startHook() {
        if (!sPrefs.getBoolean("auto_like", false)) return

        Log.d("startHook: AutoLike")

        val likeId = getId("frame1")
        val detailClass =
            "tv.danmaku.bili.ui.video.api.BiliVideoDetail".findClassOrNull(mClassLoader) ?: return

        val hooker: Hooker = fun(param) {
            val sec = param.thisObject ?: return
            val detail = sec.javaClass.findFieldByExactType(detailClass)?.get(sec)
            val avid = detail?.getLongField("mAvid") ?: return
            if (likedVideos.contains(avid)) return
            likedVideos.add(avid)
            val requestUser = detail.getObjectField("mRequestUser")
            val like = requestUser?.getIntField("mLike")
            val likeView = sec.javaClass.declaredFields.map {
                sec.getObjectField(it.name)
            }.filter {
                View::class.java.isInstance(it)
            }.map {
                it as View
            }.first {
                it.id == likeId
            }
            if (like == 0) {
                sec.callMethod("onClick", likeView)
            }
        }
        instance.partySectionClass?.hookAfterMethod(
            instance.partyLikeMethod(),
            Object::class.java,
            hooker = hooker
        )

        instance.sectionClass?.hookAfterMethod(
            instance.likeMethod(),
            Object::class.java,
            hooker = hooker
        )
    }
}
