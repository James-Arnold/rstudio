/*
 * Win32ParentProcessMonitor.cpp
 *
 * Copyright (C) 2009-11 by RStudio, Inc.
 *
 * This program is licensed to you under the terms of version 3 of the
 * GNU Affero General Public License. This program is distributed WITHOUT
 * ANY EXPRESS OR IMPLIED WARRANTY, INCLUDING THOSE OF NON-INFRINGEMENT,
 * MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE. Please refer to the
 * AGPL (http://www.gnu.org/licenses/agpl-3.0.txt) for more details.
 *
 */
#include <core/system/ParentProcessMonitor.hpp>

#include <windows.h>
#include <stdio.h>

#include <core/Log.hpp>
#include <core/SafeConvert.hpp>
#include <core/Error.hpp>

namespace core {
namespace parent_process_monitor {

Error wrapFork(boost::function<void()> func)
{
   func();

   return Success();
}

ParentTermination waitForParentTermination()
{
   return ParentTerminationNoParent;
}

} // namespace parent_process_monitor
} // namespace core
