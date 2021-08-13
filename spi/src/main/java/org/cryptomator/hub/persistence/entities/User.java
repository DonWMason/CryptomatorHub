package org.cryptomator.hub.persistence.entities;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "user")
@NamedQuery(name = "User.count", query = "SELECT COUNT(u) FROM User u")
public class User {

	@Id
	@Column(name = "id", nullable = false)
	private String id;

	@OneToMany(mappedBy = "user", cascade = {CascadeType.PERSIST}, orphanRemoval = true, fetch = FetchType.LAZY)
	private Set<Device> devices = new HashSet<>();

	@OneToMany(mappedBy = "user", cascade = {CascadeType.PERSIST}, orphanRemoval = true, fetch = FetchType.LAZY)
	private Set<Vault> vaults = new HashSet<>();

	@Column(name = "name", nullable = false)
	private String name;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Set<Device> getDevices() {
		return devices;
	}

	public void setDevices(Set<Device> devices) {
		this.devices.clear();
		this.devices.addAll(devices);
	}

	public Set<Vault> getVaults() {
		return vaults;
	}

	public void setVaults(Set<Vault> vaults) {
		this.vaults.clear();
		this.vaults.addAll(vaults);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return "User{" +
				"id='" + id + '\'' +
				", devices=" + devices.size() +
				", vaults=" + vaults.size() +
				", name='" + name + '\'' +
				'}';
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		User user = (User) o;
		return Objects.equals(id, user.id)
				&& Objects.equals(devices, user.devices)
				&& Objects.equals(vaults, user.vaults)
				&& Objects.equals(name, user.name);
	}

    /*@Override
    public int hashCode() {
        return Objects.hash(id, devices, vaults, name);
    }*/
}