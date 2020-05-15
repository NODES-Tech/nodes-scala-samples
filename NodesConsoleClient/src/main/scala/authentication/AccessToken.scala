package authentication

object AccessToken {
  private var token = "NOT SET"
  def setToken(t: String) = token = t
  def getToken         = token
  def getBaseUrl         = "https://api-test-mock.nodesmarket.com"
}
