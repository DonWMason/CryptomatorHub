package org.cryptomator.hub.entities;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import io.quarkus.panache.common.Parameters;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.transaction.Transactional;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "user")
@NamedQuery(name = "User.includingDevices", query = "SELECT u FROM User u LEFT JOIN FETCH u.devices")
@NamedQuery(name = "User.includingDevicesAndVaults",
		query = """
					SELECT DISTINCT u
					FROM User u
						LEFT JOIN FETCH u.devices d
						LEFT JOIN FETCH d.access
				""")
@NamedQuery(name = "User.withDevicesAndAccess",
		query = """
					SELECT u
					FROM User u
						LEFT JOIN FETCH u.devices d
						LEFT JOIN FETCH d.access a
						LEFT JOIN FETCH a.vault
					WHERE u.id = :userId
				""")
public class User extends PanacheEntityBase {

	@Id
	@Column(name = "id", nullable = false)
	public String id;

	@OneToMany(mappedBy = "owner", cascade = {CascadeType.PERSIST}, orphanRemoval = true, fetch = FetchType.LAZY)
	public Set<Device> devices = new HashSet<>();

	@OneToMany(mappedBy = "owner", cascade = {CascadeType.PERSIST}, orphanRemoval = true, fetch = FetchType.LAZY)
	public Set<Vault> ownedVaults = new HashSet<>();

	@ManyToMany(mappedBy = "members")
	public Set<Vault> sharedVaults = new HashSet<>();

	@Column(name = "name", nullable = false)
	public String name;

	@Column(name = "picture_url")
	public String pictureUrl;

	@Override
	public String toString() {
		return "User{" +
				"id='" + id + '\'' +
				", devices=" + devices.size() +
				", ownedVaults=" + ownedVaults.size() +
				", name='" + name + '\'' +
				'}';
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		User user = (User) o;
		return Objects.equals(id, user.id)
				&& Objects.equals(name, user.name)
				&& Objects.equals(pictureUrl, user.pictureUrl);
	}

    @Override
    public int hashCode() {
        return Objects.hash(id, name, pictureUrl);
    }

	// --- data layer queries ---

	@Transactional(Transactional.TxType.REQUIRED)
	public static void createOrUpdate(String id, String name, String pictureUrl) {
		User user = findById(id);
		if (user == null) {
			user = new User();
			user.id = id;
		}
		user.name = name;
		user.pictureUrl = pictureUrl;
		user.persist();
	}

	public static List<User> getAllWithDevices() {
		return list("#User.includingDevices");
	}

	public static List<User> getAllWithDevicesAndAccess() {
		return list("#User.includingDevicesAndVaults");
	}

	public static User getWithDevicesAndAccess(String userId) {
		return find("#User.withDevicesAndAccess", Parameters.with("userId", userId)).firstResult();
	}

}
