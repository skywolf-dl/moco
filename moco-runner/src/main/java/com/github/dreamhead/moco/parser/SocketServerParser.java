package com.github.dreamhead.moco.parser;

import com.github.dreamhead.moco.MocoConfig;
import com.github.dreamhead.moco.SocketServer;
import com.github.dreamhead.moco.internal.ActualSocketServer;
import com.github.dreamhead.moco.parser.model.SessionSetting;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

public class SocketServerParser extends BaseParser<SocketServer> {
    @Override
    protected SocketServer createServer(ImmutableList<SessionSetting> sessionSettings, Optional<Integer> port, MocoConfig... configs) {
        SocketServer server = ActualSocketServer.createLogServer(port);
        for (SessionSetting session : sessionSettings) {
            logger.debug("Parse session: {}", session);

            session.bindTo(server);
        }

        return server;
    }
}
