# MDDrawerLayout
Simple Template for a Drawer Layout

It follows this convention, however the "Master" in this case would be Full screen, and the detail slides out only when
the take picture button's OnClickListener is invoked, however this could be triggered with any event/callback.

![](http://developer.android.com/images/fundamentals/fragments.png)

To use, you'll want to do these things:

1.  Replace the master detail fragments with your own logic.
2.  Change the callbacks to fill with the appropriate data.
3.  Adjust the Slide Out drawer to slide, not slide per your requirements.
4.  Optionally, refactor the names of the Activities and Fragments as well.

Really this is very very close to the default template implementation of the Master detail, with only a few tweaks to make 
tablets a slide out.
