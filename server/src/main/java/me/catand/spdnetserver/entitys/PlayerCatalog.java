package me.catand.spdnetserver.entitys;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "player_catalogs")
public class PlayerCatalog {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "player_id")
	private Long playerId;

	@Column(name = "catalog_type", nullable = false, length = 50)
	private String catalogType;

	@Column(name = "item_class", nullable = false, length = 255)
	private String itemClass;

	@Column(name = "seen", nullable = false)
	private boolean seen;

	@Column(name = "use_count", nullable = false)
	private int useCount;
}
