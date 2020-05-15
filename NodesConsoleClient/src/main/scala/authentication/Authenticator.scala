package authentication
import java.net.URL
import authentication.AccessToken
import org.apache.oltu.oauth2.client._
import org.apache.oltu.oauth2.client.request.OAuthClientRequest
import org.apache.oltu.oauth2.common.message.types._
import org.apache.oltu.oauth2.client.OAuthClient
import org.apache.oltu.oauth2.client.URLConnectionClient
import org.apache.oltu.oauth2.client.response.OAuthAccessTokenResponse

class Authenticator {
  def performAuthenication(clientId:String, clientSecret:String) = {

    val request = OAuthClientRequest
      .tokenLocation("https://login.microsoftonline.com/nodestechdev.onmicrosoft.com/oauth2/v2.0/token/")
      .setGrantType(GrantType.CLIENT_CREDENTIALS)
      .setClientId(clientId)
      .setClientSecret(clientSecret)
      .setRedirectURI("https://portal-test.nodesmarket.com")
      .setScope("https://nodestechdev.onmicrosoft.com/devNodesApi/.default")
      .buildBodyMessage();

      val oAuthClient = new OAuthClient(new URLConnectionClient)
      val oauthResponse = oAuthClient.accessToken(request)

      System.out.println("Access Token: " + oauthResponse.getAccessToken)
      System.out.println("Expires In: " + oauthResponse.getExpiresIn)
      AccessToken.setToken(oauthResponse.getAccessToken)
  }
}
