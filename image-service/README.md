# OpenCV and Java on AWS Lambda
This is an example of using OpenCV with Java on AWS Lambda.

## Getting Setup

### Prerequisites
Build and test locally:
- Java 21+
- Maven
- Docker

Deploy to AWS:
- NodeJS 20+
- AWS Credentials

### Unit tests
```shell
mvn test
```

### Package the application (maven shade)
```shell
mvn package
```

### Run the application locally
Run the static void main web server:
```
com.mycodefu.LocalRunner.main();
```
Then navigate to http://localhost:8001/ in your browser.

### Deploy the application to AWS
```shell
./build-deploy.sh
```
(you might need to adjust the profile used - I use the 'personal' profile)

## Why?
#### Why OpenCV?
OpenCV is a powerful computer vision library that is widely used
in industry and academia. It is a great tool for image processing
and computer vision tasks, and it is well supported on many
platforms, including mobile and small devices through to large 
servers.

#### Why Java and OpenCV?
Java is the native language for the OpenCV Android SDK, and it's a
popular language for many other purposes.
It is also a language that is well supported on AWS Lambda, and
compiles natively using just in time compilation, tuning its
performance to the specific hardware it is running on and to 
the specific task it is performing.

#### Why AWS Lambda?
Just for fun :). 

I was actually very pleasantly surprised by the performance level
on AWS Lambda, and the ease of deployment and scaling.

However in practice I would suggest if you were to seriously deploy
a server-side computer vision application, you would probably want 
to use a containerised platform.

This application could easily adapt to that, and actually has a little
web server built in for local testing that you could use as a basis
for a more complex application.

I do seriously like the ability to run an application either serverless 
or in a container, and the ability to scale it up and down as needed.

One benefit of using AWS Lambda is that when it is not in use, it
is not costing you anything. This is a great way to run a service
that is not used all the time, or that is used sporadically.

The major downside, especially with Java, is the cold-start time.
This is the time it takes to start up the JVM and load the application
into memory. This can be quite long, and is not suitable for all
applications. You'll notice that when you first load the web page.

## Background
#### Why use JNI not the new Foreign Function & Memory API?
I looked at that at first, but read that it may not be ready for C++
usage yet. 
All of the resources I found indicated to use JNI, so I went with that.
In the future it could be a very interesting thing to try though,
and might improve performance for interop with Java.

#### Building Java Docker lambdas
https://docs.aws.amazon.com/lambda/latest/dg/java-image.html
https://docs.aws.amazon.com/lambda/latest/dg/images-create.html

Repo for lambda java image:
https://gallery.ecr.aws/lambda/java

#### Using SnapStart
A future enhancement could be to try using Java SnapStart to reduce cold start times. 
https://aws.amazon.com/blogs/compute/re-platforming-java-applications-using-the-updated-aws-serverless-java-container/

#### Building OpenCV with Java JNI bindings
You can build OpenCV with Java bindings by following the instructions here:
https://delabassee.com/OpenCVJava/
(if you don't want to use something like this prebundled Maven dep)

I found the precompiled OpenPNP library to be awesome, and up to date with the latest OpenCV version.
It worked out of the box on Mac, Windows and Linux.

#### Interesting ways to run existing java frameworks in lambda
If you have an existing Java application that you want to run on AWS Lambda, you can use the AWS Serverless Java Container:
https://github.com/aws/serverless-java-container/tree/main

I'd be a little cautious with this though, as such applications may have very long cold start times.