package trades
import authentication.{AccessToken, Authenticator}
import java.net.URL

import play.api.libs.json._
import play.api.libs.functional.syntax._

// Sample that collects Tades from NODES and parses the data structures with lookup on secondary references

case class TradeLink(href: String, title: String)
case class Trade(dealId: String, status: String, periodFrom: String, periodTo: String, buyOrSell:String, quantiry:Double, price: Double, links:List[TradeLink])
case class TradeRecord(owner:String, creator: String, assetPortfolio:String,trade: Trade)


object Trades {

  def extactJsonFromUrl(url:String): JsValue= {
    var cxn = new URL(url).openConnection
    cxn.addRequestProperty("Authorization", "Bearer " + AccessToken.getToken)
    cxn.addRequestProperty("Accept", "application/json")
    val in = scala.io.Source.fromInputStream(cxn.getInputStream)
    val rawJso=in.getLines.mkString
    val json: JsValue = Json.parse(rawJso)
    in.close
    return json
  }
  def main(args: Array[String]): Unit = {
    //Sample client/secret only validates against Mock data server.
    new Authenticator().performAuthenication("740c47c0-4b9b-435f-a4c8-c82a63fbf78c",
      ".qxP-G3qgCk2Ke08l~67utdHf3YEKTo8R~")

    println("Collecting trades")
    val json = extactJsonFromUrl(AccessToken.getBaseUrl + "/trades")

    //Parse  into JsValue list
    val trList: List[JsValue] = (json \ "items").as[List[JsValue]]

    //Mapper that matches JSON
    implicit val tradeLinkReads: Reads[TradeLink] = (
      (JsPath \ "href").read[String] ~
        (JsPath \ "title").read[String]
      ) (TradeLink.apply _)


    implicit val tradeReads: Reads[Trade] = (
      (JsPath \ "id").read[String] ~
        (JsPath \ "status").read[String] ~
        (JsPath \ "periodFrom").read[String] ~
        (JsPath \ "periodTo").read[String] ~
        (JsPath \ "side").read[String] ~
        (JsPath \ "quantity").read[Double] ~
        (JsPath \ "unitPrice").read[Double] ~
        (JsPath \ "links").read[List[TradeLink]]
      ) (Trade.apply _)


    def lookupHref(href: String): String = {
      val hrefJson = extactJsonFromUrl(AccessToken.getBaseUrl + href)
      val name =
        try {
          (hrefJson \ "name").as[String](Reads.minLength(3))
        }
        catch{
          case _ => (hrefJson \ "email").as[String](Reads.minLength(3))  //Simplified check since USer did not have name parameter
        }
      name
    }

    def lookupData(field: String, list: List[TradeLink]): String = {
      for (el <- list) {
        if (el.title == field)
          return lookupHref(el.href)
      }
      ""
    }

    for (t <- trList) {
      val tradeObj = t.as[Trade]
      val tr = TradeRecord(
        lookupData("OwnerOrganization", tradeObj.links),
        lookupData("CreatedByUser", tradeObj.links),
        lookupData("AssetPortfolio", tradeObj.links),
        tradeObj)
       println(tr)
    }
  }


}
