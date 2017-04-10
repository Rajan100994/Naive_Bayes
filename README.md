Authors : 
1) Rajan Jhaveri
2) Varun Dani

Language used is Java:

How To Compile:
=>On the command prompt type 

javac PreProcessor.java


How to run : 
java NaiveBayes /Users/rajan/Desktop/news/news_items/train /Users/rajan/Desktop/news/news_items/test

Accuracy : 

Here we have implemented the Naive Bayes Classifier in such a way that it will consider all the categories that are in the training as well as the testing data. If there will be files in testing data that have labels which are not considered in the training data then we get a usage error. We have also incorporated Laplace smoothing so that we don’t get 0 probability only because one of the words not being present in the training data. We can see that as the number of folders in consideration increase, the accuracy decreases a little every time. This is because more the training data with various labels, the tougher it becomes for the classifier. Also here we do not consider word sense disambiguation so it is a little difficult to get good accuracy. If Natural Language Processing is applied, we can get much better accuracy.
