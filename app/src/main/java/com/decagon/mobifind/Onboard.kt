//package com.decagon.mobifind
//
//import android.animation.Animator
//import android.animation.AnimatorListenerAdapter
//import android.animation.AnimatorSet
//import android.animation.ObjectAnimator
//import android.content.Intent
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import android.widget.ImageView
//import androidx.core.app.FrameMetricsAggregator.ANIMATION_DURATION
//import androidx.leanback.app.OnboardingSupportFragment
//import com.decagon.mobifind.utils.SharedPreferenceUtil
//
//class Onboard : OnboardingSupportFragment() {
//    private lateinit var contentView: ImageView
//    override fun getPageCount(): Int {
//        return 3
//    }
//
//    override fun getPageTitle(pageIndex: Int): CharSequence {
//        return when(pageIndex){
//            0 -> "Tracking"
//            1-> "Tracker"
//            2-> "Remove From List"
//            else -> "Welcome"
//        }
//    }
//
//    override fun getPageDescription(pageIndex: Int): CharSequence {
//        return when(pageIndex){
//            0 -> "Track contacts on mobifind in real-time on the map and navigate to where they are"
//            1-> "Grant a contact permission to keep track of your position"
//            2-> "Swipe to delete from your tracker list"
//            else -> "Welcome"
//        }
//    }
//
//    override fun onCreateEnterAnimation(): Animator =
//        ObjectAnimator.ofFloat(contentView, View.SCALE_X, 0.2f, 1.0f)
//            .setDuration(ANIMATION_DURATION.toLong())
//
//    override fun onPageChanged(newPage: Int, previousPage: Int) {
//        // Create a fade-out animation used to fade out previousPage and, once
//        // done, swaps the contentView image with the next page's image.
//        val pageImages = arrayListOf(R.drawable.tracking, R.drawable.tracker, R.drawable.delete)
//        val fadeOut = ObjectAnimator.ofFloat(contentView, View.ALPHA, 1.0f, 0.0f)
//            .setDuration(ANIMATION_DURATION.toLong())
//            .apply {
//                addListener(object : AnimatorListenerAdapter() {
//
//                    override fun onAnimationEnd(animation: Animator) {
//                        contentView.setImageResource(pageImages[newPage])
//                        contentView.alpha = 0.4f
//                    }
//                })
//            }
//        // Create a fade-in animation used to fade in nextPage
//        val fadeIn = ObjectAnimator.ofFloat(contentView, View.ALPHA, 0.0f, 1.0f)
//            .setDuration(ANIMATION_DURATION.toLong())
//        // Create AnimatorSet with our fade-out and fade-in animators, and start it
//        AnimatorSet().apply {
//            playSequentially(fadeOut, fadeIn)
//            start()
//        }
//    }
//
//    override fun onCreateBackgroundView(inflater: LayoutInflater?, container: ViewGroup?): View? {
//        return ImageView(context).apply {
//            setImageResource(R.drawable.green)
//            contentView = this
//            scaleType = ImageView.ScaleType.FIT_XY
//        }
//    }
//
//    override fun onCreateContentView(inflater: LayoutInflater?, container: ViewGroup?): View? {
//        return ImageView(context).apply {
//            scaleType = ImageView.ScaleType.CENTER_INSIDE
//            setImageResource(R.drawable.tracking)
//            setPadding(0, 32, 0, 32)
//            contentView = this
//        }
//    }
//
//    override fun onCreateForegroundView(inflater: LayoutInflater?, container: ViewGroup?): View? {
//        return null
//    }
//
//    override fun onFinishFragment() {
//        super.onFinishFragment()
//      //  SharedPreferenceUtil.setOnboardViewedState(requireContext(),true)
//        requireActivity().finish()
//    }
//
//    override fun onProvideTheme(): Int = R.style.Theme_Leanback_Onboarding
//}