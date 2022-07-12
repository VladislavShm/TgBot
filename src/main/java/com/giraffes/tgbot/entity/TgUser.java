package com.giraffes.tgbot.entity;

import com.giraffes.tgbot.converter.jpa.LocationAttributeMapConverter;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.Hibernate;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

@Getter
@Setter
@Entity
@ToString
@Table(name = "tg_user")
public class TgUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "chat_id")
    private String chatId;

    @Column(name = "location")
    @Enumerated(EnumType.STRING)
    private Location location;

    @Lob
    @Column(name = "location_attributes")
    @Convert(converter = LocationAttributeMapConverter.class)
    public Map<LocationAttribute, String> locationAttributes = new HashMap<>();

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invited_by")
    private TgUser invitedBy;

    @Column(name = "wallet")
    private String wallet;

    @Column(name = "wallet_confirmed")
    private boolean walletConfirmed = false;

    @Column(name = "kicked")
    private boolean kicked;

    @Column(name = "locale")
    private Locale locale;

    @CreationTimestamp
    private LocalDateTime createDateTime;

    @UpdateTimestamp
    private LocalDateTime updateDateTime;

    @Transient
    private boolean justCreated = false;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        TgUser tgUser = (TgUser) o;

        return Objects.equals(id, tgUser.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
