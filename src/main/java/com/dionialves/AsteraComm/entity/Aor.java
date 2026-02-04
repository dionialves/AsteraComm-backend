package com.dionialves.AsteraComm.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.Objects;

@Entity
@Table(name = "ps_aors")
public class Aor {

    @Id
    @Column(name = "id", length = 40)
    private String id;

    @Column(name = "contact", columnDefinition = "TEXT")
    private String contact;

    @Column(name = "default_expiration")
    private Integer defaultExpiration;

    @Column(name = "max_contacts")
    private Integer maxContacts;

    @Column(name = "minimum_expiration")
    private Integer minimumExpiration;

    @Column(name = "remove_existing", length = 40)
    private String removeExisting;

    @Column(name = "qualify_frequency")
    private Integer qualifyFrequency;

    @Column(name = "authenticate_qualify", length = 40)
    private String authenticateQualify;

    @Column(name = "maximum_expiration")
    private Integer maximumExpiration;

    @Column(name = "outbound_proxy", columnDefinition = "TEXT")
    private String outboundProxy;

    @Column(name = "support_path", length = 40)
    private String supportPath;

    @Column(name = "remove_unavailable", length = 40)
    private String removeUnavailable;

    @Column(name = "qualify_2xx_only", length = 40)
    private String qualify2xxOnly;

    public Aor() {}

    public Aor(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public Integer getDefaultExpiration() {
        return defaultExpiration;
    }

    public void setDefaultExpiration(Integer defaultExpiration) {
        this.defaultExpiration = defaultExpiration;
    }

    public Integer getMaxContacts() {
        return maxContacts;
    }

    public void setMaxContacts(Integer maxContacts) {
        this.maxContacts = maxContacts;
    }

    public Integer getMinimumExpiration() {
        return minimumExpiration;
    }

    public void setMinimumExpiration(Integer minimumExpiration) {
        this.minimumExpiration = minimumExpiration;
    }

    public String getRemoveExisting() {
        return removeExisting;
    }

    public void setRemoveExisting(String removeExisting) {
        this.removeExisting = removeExisting;
    }

    public Integer getQualifyFrequency() {
        return qualifyFrequency;
    }

    public void setQualifyFrequency(Integer qualifyFrequency) {
        this.qualifyFrequency = qualifyFrequency;
    }

    public String getAuthenticateQualify() {
        return authenticateQualify;
    }

    public void setAuthenticateQualify(String authenticateQualify) {
        this.authenticateQualify = authenticateQualify;
    }

    public Integer getMaximumExpiration() {
        return maximumExpiration;
    }

    public void setMaximumExpiration(Integer maximumExpiration) {
        this.maximumExpiration = maximumExpiration;
    }

    public String getOutboundProxy() {
        return outboundProxy;
    }

    public void setOutboundProxy(String outboundProxy) {
        this.outboundProxy = outboundProxy;
    }

    public String getSupportPath() {
        return supportPath;
    }

    public void setSupportPath(String supportPath) {
        this.supportPath = supportPath;
    }

    public String getRemoveUnavailable() {
        return removeUnavailable;
    }

    public void setRemoveUnavailable(String removeUnavailable) {
        this.removeUnavailable = removeUnavailable;
    }

    public String getQualify2xxOnly() {
        return qualify2xxOnly;
    }

    public void setQualify2xxOnly(String qualify2xxOnly) {
        this.qualify2xxOnly = qualify2xxOnly;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Aor aor = (Aor) o;
        return Objects.equals(id, aor.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
