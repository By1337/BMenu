package dev.by1337.bmenu.menu.command;

import dev.by1337.bmenu.animation.Animator;
import dev.by1337.bmenu.command.Commands;
import dev.by1337.bmenu.command.ExecuteContext;
import dev.by1337.bmenu.command.argument.ArgumentParamsMap;
import dev.by1337.bmenu.command.argument.ArgumentSlots;
import dev.by1337.bmenu.hook.BungeeCordMessageSender;
import dev.by1337.bmenu.hook.VaultHook;
import dev.by1337.bmenu.loader.MenuLoader;
import dev.by1337.bmenu.menu.Menu;
import dev.by1337.bmenu.slot.SlotContent;
import dev.by1337.bmenu.slot.SlotFactory;
import dev.by1337.bmenu.slot.component.MenuClickType;
import dev.by1337.cmd.Command;
import dev.by1337.cmd.CommandMsgError;
import dev.by1337.cmd.argument.ArgumentCommand;
import dev.by1337.cmd.argument.ArgumentString;
import dev.by1337.cmd.argument.ArgumentStrings;
import dev.by1337.core.command.bcmd.CommandError;
import dev.by1337.core.command.bcmd.argument.ArgumentComponents;
import dev.by1337.core.command.bcmd.argument.ArgumentDouble;
import dev.by1337.core.command.bcmd.argument.ArgumentInt;
import dev.by1337.core.command.bcmd.argument.ArgumentSound;
import dev.by1337.core.util.text.minimessage.MiniMessage;
import dev.by1337.yaml.YamlMap;
import dev.by1337.yaml.codec.YamlCodec;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import net.kyori.adventure.util.Ticks;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Objects;


public class MenuCommands {
    private static final Command<ExecuteContext> LOOKUP;

    public static Command<ExecuteContext> getCommands() {
        return LOOKUP;
    }

    static {
        LOOKUP = new Command<ExecuteContext>("root")
                .and(playerOutput().map(c -> c.menu.viewer()))
                .and(voidOutput().map(v -> null))
                .and(menuControl())
                .and(slotCommands().map(c -> c.item))
                .and(misc())

        ;
    }


    private static Command<SlotContent> slotCommands() {
        return new Command<SlotContent>("root")
                .sub(new Command<SlotContent>("[rebuild]")
                        .executor((v, args) -> {
                            if (v == null) throw new CommandMsgError("[rebuild] доступен только для предмета!");
                            v.setDirty(true); //todo надо наверное сбросить локальные плейсы
                        })
                )
                .sub(new Command<SlotContent>("[update]")
                        .executor((v, args) -> {
                            if (v == null) throw new CommandMsgError("[update] доступен только для предмета!");
                            v.setDirty(true);
                        })
                )
                .sub(new Command<SlotContent>("[die]")
                        .executor((v, args) -> {
                            if (v == null) throw new CommandMsgError("[die] доступен только для предмета!");
                            v.markRemoved();
                        })
                )
                .sub(new Command<SlotContent>("[set_local]")
                        .argument(new ArgumentString<>("param"))
                        .argument(new ArgumentStrings<>("value"))
                        .executor((v, args) -> {
                            String param = (String) args.getOrThrow("param", "Use: [set_local] <param> <value>");
                            String value = (String) args.getOrThrow("value", "Use: [set_local] <param> <value>");
                            if (v == null) throw new CommandMsgError("[set_local] доступен только для предмета!");
                            v.setPlaceholder(param, () -> value);
                        })
                )
                ;
    }

    private static Command<ExecuteContext> menuControl() {
        return new Command<ExecuteContext>("root")
                .sub(new Command<ExecuteContext>("[CLOSE]")
                        .aliases("[close]")
                        .executor((v, args) -> v.menu.viewer().closeInventory())
                )
                .sub(new Command<ExecuteContext>("[BACK_OR_CLOSE]")
                        .aliases("[back_or_close]")
                        .argument(new ArgumentStrings<>("commands"))
                        .executor((v, args) -> {
                            var previousMenu = v.menu.previousMenu();
                            if (previousMenu != null) {
                                if (args.containsKey("commands"))
                                    runIn((String) args.get("commands"), previousMenu, v.menu.loader(), v);
                                previousMenu.reopen();
                            } else {
                                v.menu.viewer().closeInventory();
                            }
                        })
                )
                .sub(new Command<ExecuteContext>("[BACK_TO_OR_CLOSE]")
                        .aliases("[back_to_or_close]")
                        .argument(new ArgumentString<>("id"))
                        .argument(new ArgumentStrings<>("commands"))
                        .executor((v, args) -> {
                            String id = (String) args.getOrThrow("id", "Use: [back_to_or_close] <id>");
                            Menu m = v.menu.previousMenu();
                            while (m != null) {
                                if (id.contains(":")) {
                                    if (Objects.equals(m.getId(), NamespacedKey.fromString(id))) break;
                                } else if (m.getId() != null) {
                                    if (Objects.equals(m.getId().getKey(), id)) break;
                                }
                                m = m.previousMenu();
                            }
                            if (m != null) {
                                if (args.containsKey("commands"))
                                    runIn((String) args.get("commands"), m, v.menu.loader(), v);
                                m.reopen();
                            } else {
                                v.menu.viewer().closeInventory();
                            }
                        })
                )
                .sub(new Command<ExecuteContext>("[BACK]")
                        .aliases("[back]")
                        .argument(new ArgumentStrings<>("commands"))
                        .executor((v, args) -> {
                            var m = Objects.requireNonNull(v.menu.previousMenu(), "does not have a previous menu!");
                            if (args.containsKey("commands"))
                                runIn((String) args.get("commands"), m, v.menu.loader(), v);
                            m.reopen();
                        })
                )
                .sub(new Command<ExecuteContext>("[OPEN]")
                        .aliases("[open]")
                        .argument(new ArgumentString<>("menu"))
                        .argument(new ArgumentStrings<>("commands"))
                        .executor((v, args) -> {
                                    String menu = (String) args.getOrThrow("menu", "Use [open] <menu id>");
                                    Menu m = v.menu.loader().create(menu, v.menu.viewer(), v.menu);
                                    if (args.containsKey("commands"))
                                        runIn((String) args.get("commands"), m, v.menu.loader(), v);
                                    v.menu.setUpperMenu(m);
                                    m.open();
                                }
                        )
                )
                .sub(new Command<ExecuteContext>("open_with_args").executor(
                        new ArgumentParamsMap<>("params"),
                        (v, params) -> {
                            if (params == null) throw new CommandMsgError("Use: open: {menu: <menu>}");
                            String menu = params.get("menu");
                            if (menu == null) throw new CommandMsgError("Use: open: {menu: <menu>}");
                            Menu m = v.menu.loader().create(menu, v.menu.viewer(), v.menu);
                            params.forEach((k, p) -> {
                                if (k.equals("menu")) return;
                                m.addArgument(k, p);
                            });
                            v.menu.setUpperMenu(m);
                            m.open();
                        })
                )
                .sub(new Command<ExecuteContext>("back_with_args").executor(
                        new ArgumentParamsMap<>("params"),
                        (v, params) -> {
                            if (params == null) throw new CommandMsgError("Use: back: {}");
                            var menu = Objects.requireNonNull(v.menu.previousMenu(), "does not have a previous menu!");
                            params.forEach(menu::addArgument);
                            menu.reopen();
                        })
                )
                .sub(new Command<ExecuteContext>("back_or_open_with_args").executor(
                        new ArgumentParamsMap<>("params"),
                        (v, params) -> {
                            if (params == null) throw new CommandMsgError("Use: back_or_open: {menu: <menu>}");
                            String menu = params.get("menu");
                            if (menu == null) throw new CommandMsgError("Use: back_or_open: {menu: <menu>}");
                            var previousMenu = v.menu.previousMenu();
                            Menu m;
                            if (previousMenu != null) {
                                m = previousMenu;
                            } else {
                                m = v.menu.loader().create(menu, v.menu.viewer(), v.menu);
                            }
                            params.forEach((k, p) -> {
                                if (k.equals("menu")) return;
                                m.addArgument(k, p);
                            });
                            if (m == previousMenu) {
                                m.reopen();
                            } else {
                                v.menu.setUpperMenu(m);
                                m.open();
                            }
                        })
                        )
                .sub(new Command<ExecuteContext>("[BACK_OR_OPEN]")
                        .aliases("[back_or_open]")
                        .argument(new ArgumentString<>("menu"))
                        .argument(new ArgumentStrings<>("commands"))
                        .executor((v, args) -> {
                            String menu = (String) args.getOrThrow("menu", "Use [back_or_open] <menu id>");
                            var previousMenu = v.menu.previousMenu();
                            if (previousMenu != null) {
                                if (args.containsKey("commands"))
                                    runIn((String) args.get("commands"), previousMenu, v.menu.loader(), v);
                                previousMenu.reopen();
                            } else {
                                Menu m = v.menu.loader().create(menu, v.menu.viewer(), v.menu);
                                if (args.containsKey("commands"))
                                    runIn((String) args.get("commands"), m, v.menu.loader(), v);
                                v.menu.setUpperMenu(m);
                                m.open();
                            }
                        })
                )
                .sub(new Command<ExecuteContext>("[REOPEN]")
                        .aliases("[reopen]")
                        .executor((v, args) -> v.menu.reopen()
                        )
                )
                ;
    }

    private static Command<Void> voidOutput() {
        return new Command<Void>("root")
                .sub(new Command<Void>("[BROADCAST]")
                        .aliases("[broadcast]")
                        .argument(new ArgumentComponents<>("msg"))
                        .executor((v, args) -> {
                                    Component msg = (Component) args.getOrDefault("msg", Component.empty());
                                    Bukkit.getOnlinePlayers().forEach(player -> player.sendMessage(msg));
                                }
                        )
                )
                .sub(new Command<Void>("[CONSOLE]")
                        .aliases("[console]")
                        .argument(new ArgumentStrings<>("cmd"))
                        .executor((v, args) -> {
                                    String cmd = (String) args.getOrThrow("cmd", "use: [console] <cmd>");
                                    Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), cmd);
                                }
                        )
                )
                ;

    }

    private static Command<Player> playerOutput() {
        return new Command<Player>("root")
                .sub(new Command<Player>("[PLAYER]")
                        .aliases("[player]")
                        .argument(new ArgumentStrings<>("cmd"))
                        .executor((v, args) -> {
                                    String cmd = (String) args.getOrThrow("cmd", "use: [player] <cmd>");
                                    v.performCommand(cmd);
                                }
                        )
                )
                .sub(new Command<Player>("[CHAT]")
                        .aliases("[chat]")
                        .argument(new ArgumentStrings<>("cmd"))
                        .executor((v, args) -> {
                                    String cmd = (String) args.getOrThrow("cmd", "use: [chat] <cmd>");
                                    v.chat(cmd);
                                }
                        )
                )
                .sub(new Command<Player>("[sound]")
                        .aliases("[SOUND]")
                        .argument(new ArgumentSound<>("sound"))
                        .argument(new ArgumentDouble<>("volume"))
                        .argument(new ArgumentDouble<>("pitch"))
                        .executor((v, args) -> {
                                    float volume = ((Number) args.getOrDefault("volume", 1F)).floatValue();
                                    float pitch = ((Number) args.getOrDefault("pitch", 1F)).floatValue();
                                    Sound sound = (Sound) args.getOrThrow("sound", "use [sound] <sound> <?volume> <?pitch>");
                                    v.playSound(v.getLocation(), sound, volume, pitch);
                                }
                        )
                )
                .sub(new Command<Player>("[MESSAGE]")
                        .aliases("[message]")
                        .argument(new ArgumentComponents<>("msg"))
                        .executor((v, args) -> {
                                    v.sendMessage((Component) args.getOrDefault("msg", Component.empty()));
                                }
                        )
                )
                .sub(new Command<Player>("[ACTION_BAR]")
                        .aliases("[action_bar]")
                        .argument(new ArgumentComponents<>("msg"))
                        .executor((v, args) -> {
                                    Component msg = (Component) args.getOrDefault("msg", Component.empty());
                                    v.sendActionBar(msg);
                                }
                        )

                )
                .sub(new Command<Player>("[ACTION_BAR_ALL]")
                        .aliases("[action_bar_all]")
                        .argument(new ArgumentComponents<>("msg"))
                        .executor((v, args) -> {
                                    Component msg = (Component) args.getOrDefault("msg", Component.empty());
                                    Bukkit.getOnlinePlayers().forEach(player -> player.sendActionBar(msg));
                                }
                        )
                )
                .sub(new Command<Player>("[TITLE]")
                        .aliases("[title]")
                        .argument(new ArgumentString<>("msg"))
                        .argument(new ArgumentInt<>("fadeIn"))
                        .argument(new ArgumentInt<>("stay"))
                        .argument(new ArgumentInt<>("fadeOut"))

                        .executor((v, args) -> {
                                    //todo хочу ArgumentComponent а тут \n
                                    String msg = (String) args.getOrThrow("msg", "Use [TITLE] <\"Title\\nSubTitle\"> <?fadeIn> <?stay> <?fadeOut>");
                                    int fadeIn = (int) args.getOrDefault("fadeIn", 10);
                                    int stay = (int) args.getOrDefault("stay", 70);
                                    int fadeOut = (int) args.getOrDefault("fadeOut", 20);

                                    String[] arr = msg.split("\\\\n", 2);
                                    v.showTitle(Title.title(
                                            MiniMessage.deserialize(arr[0]),
                                            arr.length == 2 ? MiniMessage.deserialize(arr[1]) : Component.empty(),
                                            Title.Times.of(
                                                    Ticks.duration(fadeIn),
                                                    Ticks.duration(stay),
                                                    Ticks.duration(fadeOut)
                                            )
                                    ));
                                }
                        )
                )
                .sub(new Command<Player>("[TITLE_ALL]")
                        .aliases("[title_all]")
                        .argument(new ArgumentString<>("msg"))
                        .argument(new ArgumentInt<>("fadeIn"))
                        .argument(new ArgumentInt<>("stay"))
                        .argument(new ArgumentInt<>("fadeOut"))
                        .executor((v, args) -> {
                                    //todo хочу ArgumentComponent а тут \n
                                    String msg = (String) args.getOrThrow("msg", "Use [TITLE_ALL] <\"Title\\nSubTitle\"> <?fadeIn> <?stay> <?fadeOut>");
                                    int fadeIn = (int) args.getOrDefault("fadeIn", 10);
                                    int stay = (int) args.getOrDefault("stay", 70);
                                    int fadeOut = (int) args.getOrDefault("fadeOut", 20);

                                    String[] arr = msg.split("\\\\n", 2);
                                    Title title = Title.title(
                                            MiniMessage.deserialize(arr[0]),
                                            arr.length == 2 ? MiniMessage.deserialize(arr[1]) : Component.empty(),
                                            Title.Times.of(
                                                    Ticks.duration(fadeIn),
                                                    Ticks.duration(stay),
                                                    Ticks.duration(fadeOut)
                                            )
                                    );
                                    Bukkit.getOnlinePlayers().forEach(player -> player.showTitle(title));
                                }
                        )

                )
                .sub(new Command<Player>("[BROADCAST]")
                        .aliases("[broadcast]")
                        .argument(new ArgumentComponents<>("msg"))
                        .executor((v, args) -> {
                                    Component msg = (Component) args.getOrDefault("msg", Component.empty());
                                    Bukkit.getOnlinePlayers().forEach(player -> player.sendMessage(msg));
                                }
                        )
                )
                ;
    }

    private static Command<ExecuteContext> misc() {
        Command<ExecuteContext> commands = new Command<>("root");
        commands.sub(new Command<ExecuteContext>("[REFRESH]")
                .aliases("[refresh]")
                .executor((v, args) -> v.menu.refresh())
        );
        commands.sub(new Command<ExecuteContext>("[SET_PARAM]")
                .aliases("[set_param]")
                .argument(new ArgumentString<>("param"))
                .argument(new ArgumentStrings<>("value"))
                .executor((v, args) -> {
                            String param = (String) args.getOrThrow("param", "Use [set_param] <param> <value>");
                            String value = (String) args.getOrThrow("value", "Use [set_param] <param> <value>");
                            v.menu.addArgument(param, value);
                        }
                )
        );
        commands.sub(new Command<ExecuteContext>("[IMPORT_PARAMS]")
                .aliases("[import_params]")
                .argument(new ArgumentString<>("item"))
                .executor((v, args) -> {
                            String param = (String) args.getOrThrow("item", "Use [import_params] <item>");
                            Menu m = v.menu;
                            SlotFactory builder = m.resolveSlotBuilder(param, m);
                            if (builder == null) {
                                throw new CommandError("No such item: '{}'. Command [import_params]", param);
                            }
                            builder.args().forEach(m::addArgument);
                        }
                )
        );
        //todo не умеет кешировать команды не из MenuCommands::getCommands
        commands.sub(new Command<ExecuteContext>("[DELAY]")
                .aliases("[delay]")
                .argument(new ArgumentInt<>("delay"))
                .argument(new ArgumentCommand<>("cmd", MenuCommands::getCommands))
                .executor((v, args) -> {
                            int delay = (int) args.getOrThrow("delay", "Use: [delay] <delay> <command>");
                            ArgumentCommand.RunnableCommand<ExecuteContext> cmd = (ArgumentCommand.RunnableCommand<ExecuteContext>) args.getOrThrow("cmd", "Use: [delay] <delay> <command>");
                            Bukkit.getScheduler().runTaskLater(v.menu.loader().plugin(), () -> cmd.run(v, s -> s), delay);
                        }
                )
        );
        commands.sub(new Command<ExecuteContext>("[run_rand]")
                .aliases("[run_rand]")
                .executor((v, args) -> {
                            Commands set = v.menu.config().commandList().getRandom();
                            if (set == null) {
                                throw new CommandError("commands_list не определён в конфиге!");
                            }
                            set.test(v, v.menu);
                        }
                )
        );
        commands.sub(new Command<ExecuteContext>("[RUN]")
                .aliases("[run]")
                .argument(new ArgumentString<>("name"))
                .executor((v, args) -> {
                            String name = (String) args.getOrThrow("name", "Use [run] <name>");
                            Commands set = v.menu.config().commandList().getByName(name);
                            if (set == null) {
                                throw new CommandError("В commands_list не пределён набор команд {}", name);
                            }
                            set.test(v, v.menu);
                        }
                )
        );
        commands.sub(new Command<ExecuteContext>("[ANIMATION_FORCE_END]")
                .aliases("[animation_force_end]")
                .executor((v, args) -> {
                            if (v.menu.animator() != null) {
                                v.menu.animator().forceEnd(v.menu.layers().getAnimationLayer(), v.menu);
                            }
                        }
                )
        );
        commands.sub(new Command<ExecuteContext>("[ANIMATION_TICK]")
                .aliases("[animation_tick]")
                .executor((v, args) -> {
                            if (v.menu.animator() != null && !v.menu.animator().isEnd()) {
                                v.menu.animator().tick(v.menu.layers().getAnimationLayer(), v.menu);
                            }
                        }
                )
        );
        commands.sub(new Command<ExecuteContext>("[FLUSH]")
                .aliases("[flush]")
                .executor((v, args) -> {
                            v.menu.flush();
                        }
                )
        );
        commands.sub(new Command<ExecuteContext>("[CONNECT]")
                .aliases("[connect]")
                .argument(new ArgumentString<>("server"))
                .executor((v, args) -> {
                            String server = (String) args.getOrThrow("server", "Use [connect] <server>");
                            BungeeCordMessageSender.connectPlayerToServer(v.menu.viewer(), server, v.menu.loader().plugin());
                        }
                )
        );
        commands.sub(new Command<ExecuteContext>("[SET_ANIMATION]")
                .aliases("[set_animation]")
                .argument(new ArgumentString<>("animation"))
                .executor((v, args) -> {
                            String animation = (String) args.getOrThrow("animation", "Use: [set_animation] <animation>");
                            Animator.AnimatorContext ctx = v.menu.config().animations().get(animation);
                            if (ctx == null) {
                                throw new CommandError("Неизвестная анимация {}", animation);
                            }
                            v.menu.setAnimator(ctx.createAnimator());
                        }
                )
        );
        commands.sub(new Command<ExecuteContext>("[GIVEMONEY]")
                .aliases("[givemoney]")
                .argument(new ArgumentDouble<>("count"))
                .executor((v, args) -> {
                            Double count = ((Number) args.getOrThrow("count", "Use: [givemoney] <count>")).doubleValue();
                            if (!VaultHook.get().isAvailable()) {
                                throw new CommandError("Economy not defined");
                            }
                            VaultHook.get().depositPlayer(v.menu.viewer(), count);
                        }
                )
        );
        commands.sub(new Command<ExecuteContext>("[TAKEMONEY]")
                .aliases("[takemoney]")
                .argument(new ArgumentDouble<>("count"))
                .executor((v, args) -> {
                            Double count = ((Number) args.getOrThrow("count", "Use: [takemoney] <count>")).doubleValue();
                            if (!VaultHook.get().isAvailable()) {
                                throw new CommandError("Economy not defined");
                            }
                            VaultHook.get().withdrawPlayer(v.menu.viewer(), count);
                        }
                )
        );

        commands.sub(new Command<ExecuteContext>("[REBUILD_SLOTS]")
                .aliases("[rebuild_slots]")
                .argument(new ArgumentSlots<>("src"))
                .executor((v, args) -> {
                    int[] src = (int[]) args.getOrThrow("src", "Use: [rebuild_slots] <slots>");
                    v.menu.rebuildItemsInSlots(src);
                })

        );
        commands.sub(new Command<ExecuteContext>("[update_slots]")
                .aliases("[UPDATE_SLOTS]")
                .argument(new ArgumentSlots<>("src"))
                .executor((v, args) -> {
                    int[] src = (int[]) args.getOrThrow("src", "Use: [update_slots] <slots>");
                    for (int slot : src) {
                        SlotContent item = v.menu.findItemInSlot(slot);
                        if (item != null) {
                            item.setDirty(true);
                        }
                    }
                })

        );
        commands.sub(new Command<ExecuteContext>("[die_slots]")
                .aliases("[DIE_SLOTS]")
                .argument(new ArgumentSlots<>("src"))
                .executor((v, args) -> {
                    int[] src = (int[]) args.getOrThrow("src", "Use: [die_slots] <slots>");
                    for (int slot : src) {
                        SlotContent item = v.menu.findItemInSlot(slot);
                        if (item != null) {
                            item.markRemoved();
                        }
                    }
                })

        );
        commands.sub(new Command<ExecuteContext>("[set_title]")
                .aliases("[SET_TITLE]")
                .argument(new ArgumentString<>("title"))
                .executor((v, args) -> {
                    String title = (String) args.getOrThrow("title", "Use: [set_title] <title>");
                    v.menu.setTitle(title);
                })

        );
        commands.sub(new Command<ExecuteContext>("[up]")
                .executor((v, args) -> {
                    var m = v.menu.upperMenu();
                    v.menu = Objects.requireNonNull(m, "has no up menu!");
                })
        );
        commands.sub(new Command<ExecuteContext>("[down]")
                .executor((v, args) -> {
                    var m = v.menu.previousMenu();
                    v.menu = Objects.requireNonNull(m, "has no down menu!");
                })
        );
        commands.sub(new Command<ExecuteContext>("[import_from_down]")
                .executor((v, args) -> {
                    var m = v.menu.previousMenu();
                    Objects.requireNonNull(m, "has no down menu!");
                    v.menu.resolvers().addResolver(m.resolvers());
                })
        );
        commands.sub(new Command<ExecuteContext>("[simulate]")
                .sub(new Command<ExecuteContext>("click")
                        .argument(new ArgumentInt<>("slot"))
                        .executor((v, args) -> {
                            int slot = (int) args.getOrThrow("slot", "use: [simulate] click <slot>");
                            var item = v.menu.findItemInSlot(slot);
                            if (item == null) throw new CommandError("has no item in slot {}", slot);
                            item.doClick(v.menu, v.menu.viewer(), MenuClickType.ANY_CLICK);
                        })
                )
        );


        Command<ExecuteContext> layerCommands = new Command<ExecuteContext>("[layer]")
                .aliases("[LAYER]");
        for (int i = 0; i < 16; i++) { // костыль чтобы можно было писать [layer] 0 <под команда>
            layerCommands.sub(createLayerCommands(i));
        }
        commands.sub(layerCommands);
        return commands;
    }

    //[layer] l1 move l2 src dest
    //[layer] l1 copy l2 src dest
    //[layer] l1 set item dest
    //[layer] l1 set -me dest
    //[layer] l1 rm slots
    //[layer] l1 clear
    private static Command<ExecuteContext> createLayerCommands(int layerIndex) {
        return new Command<ExecuteContext>(Integer.toString(layerIndex))
                .sub(new Command<ExecuteContext>("move")
                        .aliases("mv")
                        .argument(new ArgumentInt<>("layer2"))
                        .argument(new ArgumentSlots<>("from"))
                        .argument(new ArgumentSlots<>("to"))
                        .executor((v, args) -> {
                            int layer2 = (int) args.getOrThrow("layer2", "Use: [layer] <layer> move <slots-from> <slots-to>");
                            int[] from = (int[]) args.getOrThrow("from", "Use: [layer] <layer> move <slots-from> <slots-to>");
                            int[] to = (int[]) args.getOrThrow("to", "Use: [layer] <layer> move <slots-from> <slots-to>");
                            SlotContent[] src = v.menu.layers().getMatrix(layerIndex);
                            SlotContent[] dest = v.menu.layers().getMatrix(layer2);
                            for (int idx = 0; idx < from.length; idx++) {
                                int fromIndex = from[idx];
                                int toIndex = to[idx];

                                if (fromIndex < 0 || fromIndex >= src.length || toIndex < 0 || toIndex >= dest.length) {
                                    throw new IndexOutOfBoundsException("Индексы 'from' или 'to' выходят за пределы меню.");
                                }
                                dest[toIndex] = src[fromIndex];
                                src[fromIndex] = null;
                            }
                        })
                )
                .sub(new Command<ExecuteContext>("copy")
                        .aliases("cpy")
                        .argument(new ArgumentInt<>("layer2"))
                        .argument(new ArgumentSlots<>("from"))
                        .argument(new ArgumentSlots<>("to"))
                        .executor((v, args) -> {
                            int layer2 = (int) args.getOrThrow("layer2", "Use: [layer] <layer> copy <slots-from> <slots-to>");
                            int[] from = (int[]) args.getOrThrow("from", "Use: [layer] <layer> copy <slots-from> <slots-to>");
                            int[] to = (int[]) args.getOrThrow("to", "Use: [layer] <layer> copy <slots-from> <slots-to>");
                            SlotContent[] src = v.menu.layers().getMatrix(layerIndex);
                            SlotContent[] dest = v.menu.layers().getMatrix(layer2);
                            for (int idx = 0; idx < from.length; idx++) {
                                int fromIndex = from[idx];
                                int toIndex = to[idx];

                                if (fromIndex < 0 || fromIndex >= src.length || toIndex < 0 || toIndex >= dest.length) {
                                    throw new CommandError("Индексы 'from' или 'to' выходят за пределы меню.");
                                }
                                dest[toIndex] = src[fromIndex];
                            }
                        })
                ).sub(new Command<ExecuteContext>("set")
                        .aliases("st")
                        .argument(new ArgumentString<>("item"))
                        .argument(new ArgumentSlots<>("slots"))
                        .executor((v, args) -> {
                            String itemID = (String) args.getOrThrow("item", "Use: [layer] <layer> set <item> <slots>");
                            int[] slots = (int[]) args.getOrThrow("slots", "Use: [layer] <layer> set <item> <slots>");
                            SlotContent[] src = v.menu.layers().getMatrix(layerIndex);
                            SlotContent item;
                            if ("-me".equals(itemID)) {
                                item = v.item;
                            } else {
                                SlotFactory builder = v.menu.resolveSlotBuilder(itemID, v.menu);
                                if (builder != null) {
                                    item = builder.buildIfVisible(v.menu);
                                } else {
                                    item = SlotContent.ofMaterial(itemID);
                                }
                            }
                            for (int slot : slots) {
                                if (slot < 0 || slot >= src.length) {
                                    throw new CommandError("слот {} за пределами меню!", slot);
                                }
                                src[slot] = item;
                            }
                        })
                ).sub(new Command<ExecuteContext>("remove")
                        .aliases("rm")
                        .argument(new ArgumentSlots<>("slots"))
                        .executor((v, args) -> {
                            int[] slots = (int[]) args.getOrThrow("slots", "Use: [layer] <layer> remove <item> <slots>");
                            SlotContent[] src = v.menu.layers().getMatrix(layerIndex);
                            for (int slot : slots) {
                                if (slot < 0 || slot >= src.length) {
                                    throw new CommandError("слот {} за пределами меню!", slot);
                                }
                                src[slot] = null;
                            }
                        })
                )
                .sub(new Command<ExecuteContext>("clear")
                        .executor((v, args) -> {
                            Arrays.fill(v.menu.layers().getMatrix(layerIndex), null);
                        })
                );
    }

    //todo json?
    private static void runIn(String rawList, Menu menu, MenuLoader loader, ExecuteContext ctx) {
        if (rawList != null && !rawList.isBlank()) {
            try (ExecuteContext newCtx = ExecuteContext.of(menu, ctx.item, "run in " + menu.getId())) {
                YamlMap yaml = YamlMap.loadFromString("data: " + rawList);
                var list = yaml.get("data").decode(YamlCodec.STRINGS).getOrThrow();
                menu.runCommands(newCtx, list);
            } catch (Exception e) {
                loader.logger().error("Failed to parse commands {}", rawList, e);
            }
        }
    }
}
