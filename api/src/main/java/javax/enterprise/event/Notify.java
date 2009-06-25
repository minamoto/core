/*
* JBoss, Home of Professional Open Source
* Copyright 2008, Red Hat Middleware LLC, and individual contributors
* by the @authors tag. See the copyright.txt in the distribution for a
* full listing of individual contributors.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
* http://www.apache.org/licenses/LICENSE-2.0
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package javax.enterprise.event;

/**
 * An enumeration that is used to declare the condition under which an observer
 * method should be called. The default behavior is to create the bean and
 * invoke the observer method synchronously.
 * 
 * @author Gavin King
 * @author Dan Allen
 */
public enum Notify
{
   /**
    * Specifies that an observer method is only called if the current instance
    * of the bean declaring the observer method already exists.
    */
   IF_EXISTS,

   /**
    * Specifies that an observer method is called synchronously.
    */
   SYNCHRONOUSLY,

   /**
    * Specifies that an observer method receives the event notifications
    * asynchronously.
    */
   ASYNCHRONOUSLY
}