/*
 * Packrat.java
 *
 * Copyright (C) 2014 by RStudio, Inc.
 *
 * Unless you have received this program directly from RStudio pursuant
 * to the terms of a commercial license agreement with RStudio, then
 * this program is licensed to you under the terms of version 3 of the
 * GNU Affero General Public License. This program is distributed WITHOUT
 * ANY EXPRESS OR IMPLIED WARRANTY, INCLUDING THOSE OF NON-INFRINGEMENT,
 * MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE. Please refer to the
 * AGPL (http://www.gnu.org/licenses/agpl-3.0.txt) for more details.
 *
 */

package org.rstudio.studio.client.packrat;

import org.rstudio.core.client.Debug;
import org.rstudio.core.client.command.CommandBinder;
import org.rstudio.core.client.command.Handler;
import org.rstudio.core.client.files.FileSystemItem;
import org.rstudio.core.client.widget.ProgressIndicator;
import org.rstudio.core.client.widget.ProgressOperationWithInput;
import org.rstudio.studio.client.RStudioGinjector;
import org.rstudio.studio.client.common.FileDialogs;
import org.rstudio.studio.client.packrat.model.PackratStatus;
import org.rstudio.studio.client.packrat.ui.PackratStatusDialog;
import org.rstudio.studio.client.server.ServerError;
import org.rstudio.studio.client.server.ServerRequestCallback;
import org.rstudio.studio.client.server.remote.RemoteServer;
import org.rstudio.studio.client.workbench.WorkbenchContext;
import org.rstudio.studio.client.workbench.commands.Commands;
import org.rstudio.studio.client.workbench.model.RemoteFileSystemContext;
import org.rstudio.studio.client.workbench.views.packages.Packages;

import com.google.gwt.core.client.JsArray;
import com.google.inject.Inject;
import com.google.inject.Provider;

public class Packrat
{
   public interface Binder extends CommandBinder<Commands, Packrat> {}

   public Packrat(Packages.Display display)
   {
      display_ = display;
      RStudioGinjector.INSTANCE.injectMembers(this);
   }
   
   @Inject
   public void initialize(
         Binder binder,
         Commands commands,
         WorkbenchContext workbenchContext,
         RemoteFileSystemContext fsContext,
         PackratStatus prStatus,
         RemoteServer server,
         PackratUtil util,
         Provider<FileDialogs> pFileDialogs) {
      
      workbenchContext_ = workbenchContext;
      fsContext_ = fsContext;
      server_ = server;
      util_ = util;
      pFileDialogs_ = pFileDialogs;
      binder.bind(commands, this);
   }

   @Handler
   public void onPackratSnapshot() 
   {
      util_.executePackratFunction("snapshot");
   }

   @Handler
   public void onPackratRestore() 
   {
      util_.executePackratFunction("restore");
   }

   @Handler
   public void onPackratClean() 
   {
      util_.executePackratFunction("clean");
   }

   @Handler
   public void onPackratBundle() 
   {
      pFileDialogs_.get().saveFile(
            "Save Bundled Packrat Project...",
            fsContext_,
            workbenchContext_.getCurrentWorkingDir(),
            "zip",
            false,
            new ProgressOperationWithInput<FileSystemItem>() {

               @Override
               public void execute(FileSystemItem input,
                                   ProgressIndicator indicator) {

                  if (input == null)
                     return;

                  indicator.onCompleted();

                  String bundleFile = input.getPath();
                  if (bundleFile == null)
                     return;

                  StringBuilder args = new StringBuilder();
                  // We use 'overwrite = TRUE' since the UI dialog will prompt
                  // us if we want to overwrite
                  args
                  .append("file = '")
                  .append(bundleFile)
                  .append("', overwrite = TRUE")
                  ;

                  util_.executePackratFunction("bundle", args.toString());
               }

            });
   }

   @Handler
   public void onPackratStatus() 
   {
      
      String projDir = workbenchContext_.getActiveProjectDir().getPath();
      
      // Ask the server for the current project status
      server_.getPackratStatus(projDir, new ServerRequestCallback<JsArray<PackratStatus>>() {
         
         @Override
         public void onResponseReceived(JsArray<PackratStatus> prStatus) {
            new PackratStatusDialog(prStatus).showModal();
         }

         @Override
         public void onError(ServerError error) {
            Debug.logError(error);
         }
         
      });
      
   }
  
   @SuppressWarnings("unused")
   private final Packages.Display display_;
   private PackratUtil util_;
   private RemoteFileSystemContext fsContext_;
   private WorkbenchContext workbenchContext_;
   private Provider<FileDialogs> pFileDialogs_;
   private RemoteServer server_;
}