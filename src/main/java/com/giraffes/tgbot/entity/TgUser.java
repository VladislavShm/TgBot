package com.giraffes.tgbot.entity;

import com.giraffes.tgbot.converter.jpa.LocationAttributeMapConverter;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.Hibernate;

import javax.persistence.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Getter
@Setter
@Entity
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invited_by")
    private TgUser invitedBy;

    @Column(name = "wallet")
    private String wallet;

    @Column(name = "kicked")
    private boolean kicked;

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

    @Override
    public String toString() {
        return getClass().getSimpleName() + "(" +
                "id = " + id + ", " +
                "name = " + name + ", " +
                "firstName = " + firstName + ", " +
                "lastName = " + lastName + ", " +
                "chatId = " + chatId + ", " +
                "location = " + location + ")";
    }
}
