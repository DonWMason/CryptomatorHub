package org.cryptomator.hub.entities;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import org.hibernate.annotations.Immutable;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.io.Serializable;

@Entity
@Immutable
@Table(name = "effective_vault_access")
public class EffectiveVaultAccess extends PanacheEntityBase {

	@EmbeddedId
	public EffectiveVaultAccessId id;

	@Embeddable
	public static class EffectiveVaultAccessId implements Serializable {

		@Column(name = "vault_id")
		public String vaultId;

		@Column(name = "authority_id")
		public String authorityId;
	}

}
