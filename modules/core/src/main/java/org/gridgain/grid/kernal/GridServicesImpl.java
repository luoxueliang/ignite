/* @java.file.header */

/*  _________        _____ __________________        _____
 *  __  ____/___________(_)______  /__  ____/______ ____(_)_______
 *  _  / __  __  ___/__  / _  __  / _  / __  _  __ `/__  / __  __ \
 *  / /_/ /  _  /    _  /  / /_/ /  / /_/ /  / /_/ / _  /  _  / / /
 *  \____/   /_/     /_/   \_,__/   \____/   \__,_/  /_/   /_/ /_/
 */

package org.gridgain.grid.kernal;

import org.gridgain.grid.*;
import org.gridgain.grid.compute.*;
import org.gridgain.grid.service.*;
import org.gridgain.grid.util.typedef.internal.*;
import org.jetbrains.annotations.*;

import java.io.*;
import java.util.*;

/**
 * {@link GridCompute} implementation.
 */
public class GridServicesImpl implements GridServices, Externalizable {
    /** */
    private static final long serialVersionUID = 0L;

    /** */
    private static final ThreadLocal<GridKernalContext> stash = new ThreadLocal<>();

    /** */
    private GridKernalContext ctx;

    /** */
    private GridProjection prj;

    /** */
    private UUID subjId;

    /**
     * Required by {@link Externalizable}.
     */
    public GridServicesImpl() {
        // No-op.
    }

    /**
     * @param ctx Kernal context.
     * @param prj Projection.
     */
    public GridServicesImpl(GridKernalContext ctx, GridProjection prj, UUID subjId) {
        this.ctx = ctx;
        this.prj = prj;
        this.subjId = subjId;
    }

    /** {@inheritDoc} */
    @Override public GridProjection projection() {
        return prj;
    }

    /** {@inheritDoc} */
    @Override public GridFuture<?> deployNodeSingleton(String name, GridService svc) {
        A.notNull(name, "name");
        A.notNull(svc, "svc");

        guard();

        try {
            return ctx.service().deployNodeSingleton(prj, name, svc);
        }
        finally {
            unguard();
        }
    }

    /** {@inheritDoc} */
    @Override public GridFuture<?> deployClusterSingleton(String name, GridService svc) {
        A.notNull(name, "name");
        A.notNull(svc, "svc");

        guard();

        try {
            return ctx.service().deployClusterSingleton(prj, name, svc);
        }
        finally {
            unguard();
        }
    }

    /** {@inheritDoc} */
    @Override public GridFuture<?> deployMultiple(String name, GridService svc, int totalCnt, int maxPerNodeCnt) {
        A.notNull(name, "name");
        A.notNull(svc, "svc");

        guard();

        try {
            return ctx.service().deployMultiple(prj, name, svc, totalCnt, maxPerNodeCnt);
        }
        finally {
            unguard();
        }
    }

    /** {@inheritDoc} */
    @Override public GridFuture<?> deployKeyAffinitySingleton(String name, GridService svc, @Nullable String cacheName,
        Object affKey) {
        A.notNull(name, "name");
        A.notNull(svc, "svc");
        A.notNull(affKey, "affKey");

        guard();

        try {
            return ctx.service().deployKeyAffinitySingleton(name, svc, cacheName, affKey);
        }
        finally {
            unguard();
        }
    }

    /** {@inheritDoc} */
    @Override public GridFuture<?> deploy(GridServiceConfiguration cfg) {
        A.notNull(cfg, "cfg");

        guard();

        try {
            return ctx.service().deploy(cfg);
        }
        finally {
            unguard();
        }
    }

    /** {@inheritDoc} */
    @Override public GridFuture<?> cancel(String name) {
        A.notNull(name, "name");

        guard();

        try {
            return ctx.service().cancel(name);
        }
        finally {
            unguard();
        }
    }

    /** {@inheritDoc} */
    @Override public GridFuture<?> cancelAll() {
        guard();

        try {
            return ctx.service().cancelAll();
        }
        finally {
            unguard();
        }
    }

    /** {@inheritDoc} */
    @Override public Collection<GridServiceDescriptor> deployedServices() {
        guard();

        try {
            return ctx.service().deployedServices();
        }
        finally {
            unguard();
        }
    }

    /**
     * <tt>ctx.gateway().readLock()</tt>
     */
    private void guard() {
        ctx.gateway().readLock();
    }

    /**
     * <tt>ctx.gateway().readUnlock()</tt>
     */
    private void unguard() {
        ctx.gateway().readUnlock();
    }

    /** {@inheritDoc} */
    @Override public void writeExternal(ObjectOutput out) throws IOException {
        out.writeObject(ctx);
    }

    /** {@inheritDoc} */
    @Override public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        stash.set((GridKernalContext)in.readObject());
    }

    /**
     * Reconstructs object on demarshalling.
     *
     * @return Reconstructed object.
     * @throws ObjectStreamException Thrown in case of demarshalling error.
     */
    private Object readResolve() throws ObjectStreamException {
        try {
            GridKernalContext ctx = stash.get();

            return ctx.grid().services();
        }
        catch (Exception e) {
            throw U.withCause(new InvalidObjectException(e.getMessage()), e);
        }
        finally {
            stash.remove();
        }
    }
}
