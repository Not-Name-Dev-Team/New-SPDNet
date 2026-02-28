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
@Table(name = "player_bestiary")
public class PlayerBestiary {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "player_id")
	private Long playerId;

	@Column(name = "bestiary_type", nullable = false, length = 50)
	private String bestiaryType;

	@Column(name = "entity_class", nullable = false, length = 255)
	private String entityClass;

	@Column(name = "seen", nullable = false)
	private boolean seen;

	@Column(name = "encountered", nullable = false)
	private int encountered;
}
