package org.springframework.security.oauth2.provider;

import junit.framework.TestCase;
import org.springframework.security.authentication.encoding.Md5PasswordEncoder;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.common.exceptions.InvalidClientException;

import static org.easymock.EasyMock.*;

public class TestAccessGrantAuthenticationProvider extends TestCase {
  private AccessGrantAuthenticationProvider provider;

  private ClientDetailsService clientDetailsService;

  @Override
  public void setUp() throws Exception {
    clientDetailsService = createMock(ClientDetailsService.class);
    provider = new AccessGrantAuthenticationProvider();
    provider.setClientDetailsService(clientDetailsService);
  }

  public void testInvalidClientSecret() {
    Authentication authentication = new AccessGrantAuthenticationToken("myClientId", "myInvalidSecret", null, null);
    BaseClientDetails clientDetails = new BaseClientDetails();
    clientDetails.setClientId("myClientId");
    clientDetails.setClientSecret("mySecret");
    expect(clientDetailsService.loadClientByClientId(eq("myClientId"))).andReturn(clientDetails);
    replay(clientDetailsService);

    try {
      provider.authenticate(authentication);
      fail("should have thown exception");
    } catch (InvalidClientException e) {
      assertEquals("Invalid client secret.", e.getMessage());
    }
  }

  public void testValidClientSecret() {
    Authentication authentication = new AccessGrantAuthenticationToken("myClientId", "mySecret", null, null);
    BaseClientDetails clientDetails = new BaseClientDetails();
    clientDetails.setClientId("myClientId");
    clientDetails.setClientSecret("mySecret");
    expect(clientDetailsService.loadClientByClientId(eq("myClientId"))).andReturn(clientDetails);
    replay(clientDetailsService);

    Authentication response = provider.authenticate(authentication);

    assertEquals("myClientId", ((AuthorizedClientAuthenticationToken)response).getClientId());
  }

  public void testHashedClientSecret() {
    Authentication authentication = new AccessGrantAuthenticationToken("myClientId", "mySecret", null, null);
    BaseClientDetails clientDetails = new BaseClientDetails();
    clientDetails.setClientId("myClientId");
    clientDetails.setClientSecret("a5beb6624e092adf7be31176c3079e64");
    expect(clientDetailsService.loadClientByClientId(eq("myClientId"))).andReturn(clientDetails);
    replay(clientDetailsService);
    provider.setPasswordEncoder(new Md5PasswordEncoder());

    Authentication response = provider.authenticate(authentication);

    assertEquals("myClientId", ((AuthorizedClientAuthenticationToken)response).getClientId());
  }

  public void testClentSecretWithASalt() {
    Authentication authentication = new AccessGrantAuthenticationToken("myClientId", "mySecret", null, null);
    BaseClientDetails clientDetails = new TestSaltedClientDetails();
    clientDetails.setClientId("myClientId");
    clientDetails.setClientSecret("3070500c9d9a1b42b0e80e166a528410");
    expect(clientDetailsService.loadClientByClientId(eq("myClientId"))).andReturn(clientDetails);
    replay(clientDetailsService);
    provider.setPasswordEncoder(new Md5PasswordEncoder());

    Authentication response = provider.authenticate(authentication);

    assertEquals("myClientId", ((AuthorizedClientAuthenticationToken)response).getClientId());
  }

  private class TestSaltedClientDetails extends BaseClientDetails implements SaltedClientSecret {
    public Object getSalt() {
      return "mySalt";
    }
  }
}
