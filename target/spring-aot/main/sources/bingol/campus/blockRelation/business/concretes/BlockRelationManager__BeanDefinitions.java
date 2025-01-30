package bingol.campus.blockRelation.business.concretes;

import bingol.campus.blockRelation.core.converter.BlockRelationConverter;
import bingol.campus.blockRelation.repository.BlockRelationRepository;
import bingol.campus.comment.repository.CommentRepository;
import bingol.campus.followRelation.repository.FollowRelationRepository;
import bingol.campus.friendRequest.repository.FriendRequestRepository;
import bingol.campus.like.repository.LikeRepository;
import bingol.campus.post.repository.PostRepository;
import bingol.campus.story.repository.StoryRepository;
import bingol.campus.student.repository.StudentRepository;
import bingol.campus.student.rules.StudentRules;
import org.springframework.aot.generate.Generated;
import org.springframework.beans.factory.aot.BeanInstanceSupplier;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.RootBeanDefinition;

/**
 * Bean definitions for {@link BlockRelationManager}.
 */
@Generated
public class BlockRelationManager__BeanDefinitions {
  /**
   * Get the bean instance supplier for 'blockRelationManager'.
   */
  private static BeanInstanceSupplier<BlockRelationManager> getBlockRelationManagerInstanceSupplier(
      ) {
    return BeanInstanceSupplier.<BlockRelationManager>forConstructor(StudentRepository.class, BlockRelationRepository.class, BlockRelationConverter.class, FriendRequestRepository.class, CommentRepository.class, LikeRepository.class, PostRepository.class, StoryRepository.class, FollowRelationRepository.class, StudentRules.class)
            .withGenerator((registeredBean, args) -> new BlockRelationManager(args.get(0), args.get(1), args.get(2), args.get(3), args.get(4), args.get(5), args.get(6), args.get(7), args.get(8), args.get(9)));
  }

  /**
   * Get the bean definition for 'blockRelationManager'.
   */
  public static BeanDefinition getBlockRelationManagerBeanDefinition() {
    RootBeanDefinition beanDefinition = new RootBeanDefinition(BlockRelationManager.class);
    beanDefinition.setInstanceSupplier(getBlockRelationManagerInstanceSupplier());
    return beanDefinition;
  }
}
