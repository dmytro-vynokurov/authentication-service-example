package com.example.datetime

import akka.actor.{Actor, ActorRefFactory, ActorSystem, Props}
import akka.io.IO
import akka.pattern.ask
import akka.util.Timeout
import spray.can.Http
import spray.client.pipelining._
import spray.http.MediaTypes._
import spray.http._
import spray.httpx.SprayJsonSupport
import spray.json.DefaultJsonProtocol
import spray.routing.RejectionHandler.Default
import spray.routing._

import scala.concurrent.Await
import scala.concurrent.duration._

case class DateTimeResult(datetime: String)
case class AuthenticationFailure(error:String)

object JsonImplicits extends DefaultJsonProtocol{
  implicit val dateTimeFormat = jsonFormat1(DateTimeResult)
  implicit val authenticationFailureFormat = jsonFormat1(AuthenticationFailure)
}


class DateTimeServiceActor extends Actor with HttpService with SprayJsonSupport{
  import JsonImplicits._

  val pipeline: SendReceive = sendReceive(context, context.system.dispatcher, Timeout(60.seconds))

  override def receive: Receive = runRoute(path("datetime"){
    get{
      headerValueByName("Authorization"){token: String=>
        detach(context.system.dispatcher) {
          val authorizationResult: HttpResponse = Await.result(pipeline(Get(s"http://localhost:8020/check?token=$token")), 20.seconds)
          authorizationResult.status match {
            case StatusCodes.OK =>
              respondWithMediaType(`application/json`) {
                respondWithStatus(StatusCodes.OK) {
                  complete {
                    DateTimeResult("2015-05-13 12:00:00")
                  }
                }
              }
            case _ => respondWithMediaType(`application/json`) {
              respondWithStatus(StatusCodes.Unauthorized){
                complete {
                  val unmarshaller: (HttpResponse) => AuthenticationFailure = unmarshal[AuthenticationFailure]
                  AuthenticationFailure(unmarshaller(authorizationResult).error)
                }
              }
            }
          }
        }
      }
    }
  })

  implicit def actorRefFactory: ActorRefFactory = context
  implicit val system = context.system
}

object DateTimeServiceRunner extends App{
  implicit val system = ActorSystem("date-time-actor-system")
  implicit val timeout = Timeout(5.seconds)

  val service = system.actorOf(Props[DateTimeServiceActor], "date-time-service")

  IO(Http) ? Http.Bind(service, interface = "localhost", port=8021)
}
