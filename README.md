
## Animated Sliding Drawer : Customizable Sliding Drawer for Android

Integrate AnimatedSlidingDrawer into your project and elevate your app’s interface with minimal setup. Check the documentation for setup instructions and examples to help you get started.

https://github.com/user-attachments/assets/3c52374b-7370-45ac-8f67-f1b08362d0ac

Transform the way users interact with your app using AnimatedSlidingDrawer—the perfect solution for a modern, engaging user interface!

#### Key Features

1. Smooth Transitions : Enjoy fluid animations as the drawer opens and closes, providing a polished user experience.

2. Content Shifting : The main content shifts dynamically, creating a cohesive feel that integrates the drawer into your app's layout.

3. Customization Options : Easily adjust the drawer’s width, animation speed, and opening direction (left or right) to fit your app’s design.

4. Intuitive Interaction : Trigger the drawer with a button click or swipe gesture for effortless navigation.


### Usage

 1) Add the dependency to your project
 2) Create Your drawer_layout.xml
 3) Implement the AnimatedSlidingDrawerBuilder in `onCreate` and set your drawer_layout.xml### Add to your project

#### settings.gradle.kts

```groovy
maven { url = uri("https://jitpack.io") }
```

#### build.gradle.kts

```groovy
dependencies {
	        implementation 'com.github.kushwaharsh:AnimatedSlidingDrawer:2.0.1'
	}

 ```

 


### Implementation

```groovy
animatedSlidingDrawer = AnimatedSlidingDrawerBuilder(this)
            .withMenuOpened(false)  // Initial state of the menu
            .withContentClickableWhenMenuOpened(true)
            .withSavedState(savedInstanceState)
            .withDragDistance(200)  // Distance for dragging
            .withRootViewScale(0.8f)  // Scale of the root view
            .withRootViewElevation(10)  // Elevation of the root view
            .withGravity(DrawerSlideGravity.RIGHT)  // Direction of the sliding menu
            .addRootTransformation(CustomRootTransformation())
            .withMenuLayout(R.layout.drawer_layout)  // Layout of the menu
            .addDragListener(object : DrawerDragListener {
                override fun onDrag(progress: Float) {
                    // Detect when drawer is closed
                    if (progress == 0f) {
                       // updateIcons(0)
                        replaceFragment(HomeFragment())
                        binding.bottomBar.selectedItemId = R.id.home
                    }
                }
            })
            .inject()

        // Bind drawer layout views
        val drawerView = animatedSlidingDrawer?.layout?.findViewById<View>(R.id.drawer_layout_root)
        if (drawerView != null) {
            drawerBinding = DrawerLayoutBinding.bind(drawerView)
        } else {
            Log.e("HomeMainActivity", "drawerView is null!")
            // Handle the null case, possibly by showing a user-friendly message or handling the error.
        }
```

### Sample App

Refer to sampleApp module to see the liberary usage example.
