package io.anuke.mindustry.world.blocks.defense;

import com.badlogic.gdx.math.MathUtils;
import io.anuke.mindustry.entities.TileEntity;
import io.anuke.mindustry.entities.Units;
import io.anuke.mindustry.graphics.Layer;
import io.anuke.mindustry.world.Tile;
import io.anuke.mindustry.world.blocks.defense.turrets.PowerTurret;
import io.anuke.ucore.core.Timers;
import io.anuke.ucore.graphics.Draw;
import io.anuke.ucore.graphics.Hue;
import io.anuke.ucore.graphics.Lines;
import io.anuke.ucore.util.Angles;
import io.anuke.ucore.util.Mathf;
import io.anuke.ucore.util.Strings;

public class RepairTurret extends PowerTurret {
	protected float repairFrac = 1f / 135f;

	public RepairTurret(String name) {
		super(name);
		powerUsed = 0.1f;
		layer2 = Layer.laser;
	}
	
	@Override
	public void setStats(){
		super.setStats();

		stats.add("repairssecond", Strings.toFixed(60f/reload * repairFrac * 100, 1) + "%");
	}
	
	@Override
	public void update(Tile tile){
		TurretEntity entity = tile.entity();

		if(entity.power.amount < powerUsed){
			return;
		}
		
		if(entity.blockTarget != null && entity.blockTarget.isDead()){
			entity.blockTarget = null;
		}
		
		if(entity.timer.get(timerTarget, targetInterval)){
			entity.blockTarget = Units.findAllyTile(tile.getTeam(),tile.worldx(), tile.worldy(), range,
					test -> test != tile && test.entity.health < test.block().health);
		}

		if(entity.blockTarget != null){
			if(Float.isNaN(entity.rotation)){
				entity.rotation = 0;
			}

			float target = entity.angleTo(entity.blockTarget);
			entity.rotation = Mathf.slerpDelta(entity.rotation, target, 0.16f);

			int maxhealth = entity.blockTarget.tile.block().health;

			//TODO
			if(entity.timer.get(100, reload) && Angles.angleDist(target, entity.rotation) < shootCone){
				entity.blockTarget.health += maxhealth * repairFrac;
				
				if(entity.blockTarget.health > maxhealth)
					entity.blockTarget.health = maxhealth;
				
				entity.power.amount -= powerUsed;
			}
		}
	}
	
	@Override
	public void drawLayer2(Tile tile){
		TurretEntity entity = tile.entity();
		TileEntity target = entity.blockTarget;
		
		if(entity.power.amount >= powerUsed && target != null && Angles.angleDist(entity.angleTo(target), entity.rotation) < 10){
			Tile targetTile = target.tile;
			float len = 4f;

			float x = tile.drawx() + Angles.trnsx(entity.rotation, len), y = tile.drawy() + Angles.trnsy(entity.rotation, len);
			float x2 = targetTile.drawx(), y2 = targetTile.drawy();

			Draw.color(Hue.rgb(138, 244, 138, (MathUtils.sin(Timers.time()) + 1f) / 14f));
			Draw.alpha(0.3f);
			Lines.stroke(4f);
			Lines.line(x, y, x2, y2);
			Lines.stroke(2f);
			Draw.rect("circle", x2, y2, 7f, 7f);
			Draw.alpha(1f);
			Lines.stroke(2f);
			Lines.line(x, y, x2, y2);
			Lines.stroke(1f);
			Draw.rect("circle", x2, y2, 5f, 5f);
			Draw.reset();
		}
	}
}