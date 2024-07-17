# MongoDB Atlas Image Search Service
This is an example of building an image search service with Java and MongoDB Atlas on AWS Lambda.

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

### Start a local MongoDB Atlas instance using Docker Compose
```shell
docker-compose up -d
```

### Set up the test dataset used in the application in a local MongoDB Atlas instance
```shell
./import-data.sh
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

\* Note: The application is deployed to AWS Lambda as a function URL. 
Once deployed the URL will be printed to the shell and you can launch the 
deployed app from that.

## Why?

#### Why MongoDB Atlas?


#### Why Java and MongoDB Atlas?
MongoDB and Java are a great combination for building scalable
and reliable applications. MongoDB Atlas is a fully managed
database service that is easy to use and scales with your needs.

Java is a great language for building server-side applications,
because of its strong typing, performance, and the wide range
of libraries and frameworks available.

MongoDB's Java driver is well supported and easy to use, and
the serialization to and from Java objects is very easy to use.

I really like Java records, and use them in this application
to represent the data objects.


#### Why run Java in an AWS Lambda?
Just for fun :). 

I was actually very pleasantly surprised by the performance level
on AWS Lambda, and the ease of deployment and scaling.

However in practice I would suggest if you were to seriously deploy
a server-side image search engine in Java, you would probably want 
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

#### Building Java Docker lambdas
https://docs.aws.amazon.com/lambda/latest/dg/java-image.html
https://docs.aws.amazon.com/lambda/latest/dg/images-create.html

Repo for lambda java image:
https://gallery.ecr.aws/lambda/java

#### Using SnapStart
A future enhancement could be to try using Java SnapStart to reduce cold start times. 
https://aws.amazon.com/blogs/compute/re-platforming-java-applications-using-the-updated-aws-serverless-java-container/
