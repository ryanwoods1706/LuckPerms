/*
 * This file is part of LuckPerms, licensed under the MIT License.
 *
 *  Copyright (c) lucko (Luck) <luck@lucko.me>
 *  Copyright (c) contributors
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in all
 *  copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  SOFTWARE.
 */

package me.lucko.luckperms.common.commands.impl.generic.other;

import me.lucko.luckperms.api.context.MutableContextSet;
import me.lucko.luckperms.common.commands.Arg;
import me.lucko.luckperms.common.commands.CommandException;
import me.lucko.luckperms.common.commands.CommandResult;
import me.lucko.luckperms.common.commands.abstraction.SubCommand;
import me.lucko.luckperms.common.commands.sender.Sender;
import me.lucko.luckperms.common.commands.utils.ArgumentUtils;
import me.lucko.luckperms.common.commands.utils.Util;
import me.lucko.luckperms.common.constants.Message;
import me.lucko.luckperms.common.constants.Permission;
import me.lucko.luckperms.common.core.model.Group;
import me.lucko.luckperms.common.core.model.PermissionHolder;
import me.lucko.luckperms.common.core.model.User;
import me.lucko.luckperms.common.data.LogEntry;
import me.lucko.luckperms.common.plugin.LuckPermsPlugin;
import me.lucko.luckperms.common.utils.Predicates;

import java.util.List;
import java.util.stream.Collectors;

public class HolderClear<T extends PermissionHolder> extends SubCommand<T> {
    public HolderClear(boolean user) {
        super("clear", "Removes all permissions, parents and meta", user ? Permission.USER_CLEAR : Permission.GROUP_CLEAR,
                Predicates.alwaysFalse(),
                Arg.list(
                        Arg.create("context...", false, "the contexts to filter by")
                )
        );
    }

    @Override
    public CommandResult execute(LuckPermsPlugin plugin, Sender sender, T holder, List<String> args, String label) throws CommandException {
        int before = holder.getNodes().size();

        MutableContextSet context = ArgumentUtils.handleContext(0, args, plugin);

        if (context.isEmpty()) {
            holder.clearNodes();
        } else {
            holder.clearNodes(context);
        }

        int changed = before - holder.getNodes().size();
        if (changed == 1) {
            Message.CLEAR_SUCCESS_SINGULAR.send(sender, holder.getFriendlyName(), Util.contextSetToString(context), changed);
        } else {
            Message.CLEAR_SUCCESS.send(sender, holder.getFriendlyName(), Util.contextSetToString(context), changed);
        }

        LogEntry.build().actor(sender).acted(holder)
                .action("clear " + args.stream().map(ArgumentUtils.WRAPPER).collect(Collectors.joining(" ")))
                .build().submit(plugin, sender);

        if (holder instanceof User) {
            save((User) holder, sender, plugin);
        } else if (holder instanceof Group) {
            save((Group) holder, sender, plugin);
        }

        return CommandResult.SUCCESS;
    }
}
