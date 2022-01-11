package com.codeka.justconduits.common.shape;

import com.codeka.justconduits.common.blocks.ConduitBlockEntity;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nonnull;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/** This is our cache of the shapes. The {@link ShapeManager} keeps a static reference to this. */
public class ShapeCache {
  private final Cache<ShapeCacheKey, VoxelShape> collisionShapeCache =
      CacheBuilder.newBuilder().maximumSize(64).expireAfterAccess(60, TimeUnit.SECONDS).build();

  private final Cache<ShapeCacheKey, VisualShape> visualShapeCache =
      CacheBuilder.newBuilder().maximumSize(64).expireAfterAccess(60, TimeUnit.SECONDS).build();

  /**
   * Gets the collision shape for the given {@link ConduitBlockEntity}, or calls the given loader to create a new
   * one (and caches it) if the shape is not cached.
   */
  @Nonnull
  public VoxelShape getCollisionShape(ConduitBlockEntity conduitBlockEntity, Callable<VoxelShape> loader) {
    try {
      return collisionShapeCache.get(new ShapeCacheKey(conduitBlockEntity), loader);
    } catch (ExecutionException e) {
      // Should not happen.
      throw new RuntimeException(e);
    }
  }

  @Nonnull
  public VisualShape getVisualShape(ConduitBlockEntity conduitBlockEntity, Callable<VisualShape> loader) {
    try {
      return visualShapeCache.get(new ShapeCacheKey(conduitBlockEntity), loader);
    } catch (ExecutionException e) {
      // Should not happen.
      throw new RuntimeException(e);
    }
  }
}
