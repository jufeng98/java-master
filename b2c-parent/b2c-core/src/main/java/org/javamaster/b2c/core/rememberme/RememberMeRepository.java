package org.javamaster.b2c.core.rememberme;

import org.javamaster.b2c.core.entity.SysRememberMe;
import org.javamaster.b2c.core.mapper.ManualSecurityMapper;
import org.javamaster.b2c.core.mapper.SysRememberMeMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.web.authentication.rememberme.PersistentRememberMeToken;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;

/**
 * @author yudong
 * @date 2019/6/12
 */
@Repository
public class RememberMeRepository implements PersistentTokenRepository {

    @Autowired
    private SysRememberMeMapper sysRememberMeMapper;
    @Autowired
    private ManualSecurityMapper securityMapper;

    @Override
    public void createNewToken(PersistentRememberMeToken token) {
        SysRememberMe sysRememberMe = new SysRememberMe();
        sysRememberMe.setSeries(token.getSeries());
        sysRememberMe.setUsername(token.getUsername());
        sysRememberMe.setToken(token.getTokenValue());
        sysRememberMe.setLastUsed(token.getDate());
        sysRememberMeMapper.insert(sysRememberMe);
    }

    @Override
    public void updateToken(String series, String tokenValue, Date lastUsed) {
        SysRememberMe sysRememberMe = new SysRememberMe();
        sysRememberMe.setSeries(series);
        sysRememberMe.setToken(tokenValue);
        sysRememberMe.setLastUsed(lastUsed);
        sysRememberMeMapper.updateByPrimaryKey(sysRememberMe);
    }

    @Override
    public PersistentRememberMeToken getTokenForSeries(String seriesId) {
        SysRememberMe sysRememberMe = sysRememberMeMapper.selectByPrimaryKey(seriesId);
        PersistentRememberMeToken meToken = new PersistentRememberMeToken(
                sysRememberMe.getUsername(),
                sysRememberMe.getSeries(),
                sysRememberMe.getToken(),
                sysRememberMe.getLastUsed()
        );
        return meToken;
    }

    @Override
    public void removeUserTokens(String username) {
        securityMapper.deleteByUsername(username);
    }
}
