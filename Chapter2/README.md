# CNN based Age, Gender & Emotion Prediction

iOS11 demo application for age and gender classification of facial images using `Vision` and `CoreML`. In this chapter, we are going to build an iOS application to detect the age, gender, and emotion of a person from the camera feed or from the user's photo gallery. We will use existing data models that were built for the same purpose using the Caffe machine learning (ML) library, and convert those models into Core ML models for the ease of use in our application. We will discuss more how Convolutional Neural Networks (CNNs) work in terms of predicting age, gender, and emotion with the sample application. This application can be useful with multiple use cases. 

A few cases are as follows: 
 - Finding what kind of photos you capture by parsing all the photos from your gallery 
 - Understanding the customer entering a location (hospital, restaurant, and so on) 
 - Figuring out the right marketing data by actually capturing emotions
 - Making cars safer by understanding the driver's emotions 
 
There are a lot of other use cases as well. Once you improve accuracy of the data model, you can figure out more and more use cases. 


<div align="center">
<img src="https://github.com/intrepidkarthi/MLmobileapps/blob/master/Chapter2/screenshot.png" alt="Age Gender classification" width="300" height="500" />
</div>

## Model

This demo is based on the age, gender and emotion neural network classifiers,
which were converted from `Caffe` models to `CoreML` models using [coremltools](https://pypi.python.org/pypi/coremltools) python package.

### Downloads

- [Age & Gender Paper](http://www.openu.ac.il/home/hassner/projects/cnn_agegender/)
- [Age prediction model](https://drive.google.com/file/d/0B1ghKa_MYL6mT1J3T1BEeWx4TWc/view?usp=sharing)
- [Gender prediction model](https://drive.google.com/file/d/0B1ghKa_MYL6mYkNsZHlyc2ZuaFk/view?usp=sharing)

- [Emotion Paper](http://www.openu.ac.il/home/hassner/projects/cnn_emotions/)
- [Emotion prediction model](https://drive.google.com/file/d/0B1ghKa_MYL6mTlYtRGdXNFlpWDQ/view?usp=sharing)

## Requirements

- Xcode 9
- iOS 11

## Installation

```sh
git clone https://github.com/intrepidkarthi/MLmobileapps.git
cd MLmobileapps/Chapter2
pod install
open Faces.xcworkspace/
```

## Conversion

Download [Caffe model](https://drive.google.com/open?id=0BydFau0VP3XSNVYtWnNPMU1TOGM)
and [deploy.prototxt](https://drive.google.com/open?id=0BydFau0VP3XSOFp4Ri1ITzZuUkk).
Links can also be found [here](https://gist.github.com/GilLevi/54aee1b8b0397721aa4b#emotion-classification-cnn---rgb).
Move downloaded files to `Covert/EmotionClassification` folder.

```sh
cd Convert
./download.sh
python age.py
python gender.py
python emotion.py
```

Download the [Age](https://drive.google.com/file/d/0B1ghKa_MYL6mT1J3T1BEeWx4TWc/view?usp=sharing),
[Gender](https://drive.google.com/file/d/0B1ghKa_MYL6mYkNsZHlyc2ZuaFk/view?usp=sharing) and
[Emotion](https://drive.google.com/file/d/0B1ghKa_MYL6mTlYtRGdXNFlpWDQ/view?usp=sharing)
CoreML models and add the files to "Resources" folder in the project's directory.

Build the project and run it on a simulator or a device with iOS 11.

## References
- [Caffe Model Zoo](https://github.com/caffe2/caffe2/wiki/Model-Zoo)
- [Apple Machine Learning](https://developer.apple.com/machine-learning/)
- [Vision Framework](https://developer.apple.com/documentation/vision)
- [CoreML Framework](https://developer.apple.com/documentation/coreml)
- [coremltools](https://pypi.python.org/pypi/coremltools)
