How  to gracefully stop Spark Streaming context.

Here is [post](http://metabroadcast.com/blog/stop-your-spark-streaming-application-gracefully) outlining how to *gracefully stop a Spark Streaming application* in Scala.

An here is java code
```
        // for graceful shutdown of the application ...
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                System.out.println("Shutting down streaming app...");
                context.stop(true, true);
                System.out.println("Shutdown of streaming app complete.");
            }
        });
```
taken from source: [Consumer.java](https://github.com/lenards/spark-cstar-canaries/blob/master/src/main/java/net/lenards/Consumer.java#L99-L107)
