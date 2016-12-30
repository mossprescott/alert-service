# Alerts

An exercise in writing asynchronous services using gRPC, Cassandra, and Spark Streaming.


## Running the server

First, start a development cassandra cluster (single node):
```bash
$ docker run -p 9042:9042 -d cassandra:3.9
```

Set up a `cqlsh` session connected to your development cluster, and create a new keyspace:
```bash
$ docker run -it --rm cassandra cqlsh 192.168.99.100:9042
...
cqlsh> CREATE KEYSPACE demo WITH REPLICATION = { 'class' : 'SimpleStrategy', 'replication_factor' : 1 };
cqlsh> CREATE TABLE demo.alerts (
         alertId int, 
         userId int, 
         heartRate int, 
         systolic int,
         diastolic int,
         PRIMARY KEY(userId, alertId));
```

Finally, run the RPC server:
```bash
$ sbt "run-main alerts.Main"
```

## Manual testing

In a second terminal, run a REPL session and use the `clients` object to create stubs for sending messages over RPC:

```bash
$ sbt console
...
scala> import alerts.rpc.clients
```

To send events:

```bash
scala> import alerts.rpc.events1._
scala> clients.events1().receiveOne(Event(12345, 100, 120, 80))
scala> clients.events1().receiveOne(Event(12345, 200, 120, 80))
scala> clients.events1().receiveOne(Event(12345, 120, 90, 60))
```

To query the generated alerts:

```bash
scala> import alerts.rpc.alerts._
scala> clients.alerts().getActiveAlertsByUser(UserId(12345))
...
res15: alerts.rpc.alerts.ManyAlerts =
alert {
  alert_id: 1
  user_id: 12345
  abnormal_heart_rate {
    heart_rate: 200
  }
}
alert {
  alert_id: 2
  user_id: 12345
  low_blood_pressure {
    heart_rate: 120
    systolic: 90
    diastolic: 60
  }
}
scala> clients.alerts().acknowledgeAlert(AlertId(12345, 1))
...
res16: alerts.rpc.alerts.AEmpty =
scala> clients.alerts().acknowledgeAlert(AlertId(12345, 2))
...
res16: alerts.rpc.alerts.AEmpty =
scala> clients.alerts().getActiveAlertsByUser(UserId(12345))
...
res17: alerts.rpc.alerts.ManyAlerts =
```

## Implementation Notes

Simple traits are used to compose the various components, so that there is no coupling of the implementations. See [AlertService.scala](src/main/scala/alerts/AlertService.scala), for example.

There is essentially no error-handling.

For simplicity, when an alert is acknowledged it is simply deleted from the database.