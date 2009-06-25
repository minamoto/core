package org.jboss.jsr299.tck.tests.activities.current;

import java.lang.annotation.Annotation;

import javax.enterprise.context.spi.Context;
import javax.enterprise.context.spi.Contextual;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.event.Observer;

import org.jboss.testharness.impl.packaging.Artifact;
import org.jboss.testharness.impl.packaging.IntegrationTest;
import org.jboss.webbeans.manager.api.WebBeansManager;
import org.jboss.webbeans.test.AbstractWebBeansTest;
import org.testng.annotations.Test;

/**
 * 
 * Spec version: 20090519
 *
 */
@Artifact
@IntegrationTest
public class JndiManagerCurrentActivityTest extends AbstractWebBeansTest
{

   static interface TestableObserver<T> extends Observer<T>
   {

      boolean isObserved();

   }


   private static class DummyContext implements Context
   {

      private boolean active = true;

      public <T> T get(Contextual<T> contextual)
      {
         return null;
      }

      public <T> T get(Contextual<T> contextual, CreationalContext<T> creationalContext)
      {
         return null;
      }

      public Class<? extends Annotation> getScopeType()
      {
         return Dummy.class;
      }

      public boolean isActive()
      {
         return active;
      }

      public void setActive(boolean active)
      {
         this.active = active;
      }

   }

   @Test
   public void testJndiManagerIsCurrentActivity()
   {
      Context dummyContext = new DummyContext();
      getCurrentManager().addContext(dummyContext);
      assert getBeans(Cow.class).size() == 1;
      WebBeansManager childActivity = getCurrentManager().createActivity();
      childActivity.setCurrent(dummyContext.getScopeType());
      assert createContextualInstance(Donkey.class).getManager().equals(childActivity);
   }

}
