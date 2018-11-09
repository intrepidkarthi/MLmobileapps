# Applying Neural Style Transfer on Photos 

In this chapter, we are going to build a complete iOS and Android application in which image transformations are applied to our own images in a fashion similar to the Instagram app. For this application, we are going to use Core ML and TensorFlow models again with the help of TensorFlow. To make this work, we will have to perform some small hacks. 

The best use case for this chapter is based on a photo-editing app called Prisma with which you can convert your images into paintings using neural networks. You can convert your image into an art form that looks like it was painted by Picasso or Salvador Dali. 



## Android

Open the code base in Android Studio.
<div align="center">
<img src="https://github.com/intrepidkarthi/MLmobileapps/blob/master/Chapter3/assets/pic1.png" alt="Without style" width="300" height="500" />
</div>

<div align="center">
<img src="https://github.com/intrepidkarthi/MLmobileapps/blob/master/Chapter3/assets/pic2.png" alt="With style" width="300" height="500" />
</div>


## iOS

Once you have your models, just import them into the Xcode project. The current setup looks for a particular set of models (wave, udnie, rain_princess, and la_muse)
<div align="center">
<img src="https://github.com/intrepidkarthi/MLmobileapps/blob/master/Chapter3/assets/pic3.png" alt="Without style" width="300" height="500" />
</div>

<div align="center">
<img src="https://github.com/intrepidkarthi/MLmobileapps/blob/master/Chapter3/assets/pic4.png" alt="With style" width="300" height="500" />
</div>

## Dependencies

* Xcode 9
* Python 2.7
* TensorFlow (1.0.0 works best with fast-style-transfer, and 1.1.0 or greater for tf-coreml)
* Fast Style Transfer: https://github.com/lengstrom/fast-style-transfer
* TensforFlow-CoreML: https://github.com/tf-coreml/tf-coreml




