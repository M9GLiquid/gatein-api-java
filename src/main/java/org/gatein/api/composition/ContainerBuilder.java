package org.gatein.api.composition;

import java.util.List;

import org.gatein.api.application.Application;
import org.gatein.api.security.Permission;

/**
 * Basic builder for {@link Container}s.
 *
 * The basic flow of calls to this should be:
 * <ul>
 *     <li>
 *         {@link ContainerBuilder#child(ContainerItem)}
 *         to add a new child object to this {@link Container}
 *     </li>
 *     <li>
 *         {@link ContainerBuilder#newCustomContainerBuilder(Container)},
 *         {@link ContainerBuilder#newColumnsBuilder()} and
 *         {@link ContainerBuilder#newRowsBuilder()}
 *         add a new {@link Container} as a child of this,
 *         and returns the builder related to this new container
 *     </li>
 *     <li>
 *         {@link ContainerBuilder#buildToParentBuilder()} when the caller has finished adding child items
 *     </li>
 *     <li>
 *         {@link ContainerBuilder#buildToTopBuilder()} to finish the work on this builder and return to the top level builder.
 *     </li>
 * </ul>
 *
 * The {@code new*Builder()} methods are very similar in behavior, differing only on the final container that it delivers. For instance,
 * a representation of a column would be a {@link Container} object with a specific template.
 *
 * @param <T> The final type that is implementing this interface.
 * @author <a href="mailto:jpkroehling+javadoc@redhat.com">Juraci Paixão Kröhling</a>
 */
public interface ContainerBuilder<T> {

    /**
     * Adds a new child to this {@link Container}
     * @param containerItem the {@link Container} item to add (can be an {@link org.gatein.api.application.Application},
     *                      for instance)
     */
    public ContainerBuilder<T> child(ContainerItem containerItem);

    /**
     * Adds the provided list of children to the existing list of children for this builder. If a null value is provided,
     * the current list of children is cleared.
     *
     * @param children    the list of {@link ContainerItem} to add to this container
     * @return this builder
     */
    public ContainerBuilder<T> children(List<ContainerItem> children);

    /**
     * Creates a new instance of {@link Container} based on the provided information, adds it as a child
     * to the parent {@link ContainerBuilder} and returns the parent {@link ContainerBuilder}.
     * <p>
     * Throws an {@link IllegalStateException} if the parent {@link ContainerBuilder} is {@code null}.
     * <p>
     * Throws an {@link IllegalStateException} if {@link #buildToTopBuilder()} or {@link #buildToParentBuilder()}
     * was already called on this {@link ContainerBuilder} instance.
     *
     * @return the parent container builder or itself if this container is placed at the top level
     * @throws IllegalStateException if the parent {@link ContainerBuilder} is {@code null}
     * @throws IllegalStateException if {@link #buildToTopBuilder()} or {@link #buildToParentBuilder()}
     *                              was already called on this {@link ContainerBuilder} instance
     */
    public ContainerBuilder<T> buildToParentBuilder();

    /**
     * Creates a new instance of {@link Container} based on the provided information and then either
     * <p>
     * (a) adds it as a child to the top level builder which is typically a {@link PageBuilder} if the
     * parent container builder is {@code null} or else
     * <p>
     * (b) adds it as a child to the parent container builder and calls {@link #buildToTopBuilder()}
     * on the parent container builder. This is is equivalent to
     * {@code buildToParentBuilder().buildToTopBuilder()}. Due to (b) the following two snippets give
     * exactly the same result:
     * <pre>
     *      //snippet 1
     *      PageBuilder myPageBuilder = ...
     *      Page myPage = myPageBuilder.newColumnsBuilder()
     *          .child(storyOfTheDayPortlet) // first column
     *          .child(productListPortlet) // second column
     *          .newRowsBuilder() // some rows in the third column
     *              .child(ch1) // first row
     *              .child(ch2) // second row
     *          .buildToParentBuilder() // add the row container to column container
     *    .buildToTopBuilder() // add columns container to page
     *    .name("myPage")
     *    ...
     *    .build();
     *
     *    //snippet 2
     *    PageBuilder myPageBuilder = ...
     *    Page myPage = myPageBuilder.newColumnsBuilder()
     *        .child(storyOfTheDayPortlet) // first column
     *        .child(productListPortlet) // second column
     *        .newRowsBuilder() // some rows in the third column
     *            .child(ch1) // first row
     *            .child(ch2) // second row
     *
     *    .buildToTopBuilder() // adds both rows container to columns container
     *                         // and columns container to page
     *    .name("myPage")
     *    ...
     *    .build();
     * </pre>
     * <p>
     * Finally, this method returns the top level builder (typically a {@link PageBuilder}).
     * <p>
     * Throws an {@link IllegalStateException} if there is no reference to the top level builder.
     * <p>
     * Throws an {@link IllegalStateException} if {@link #buildToTopBuilder()} or {@link #buildToParentBuilder()}
     * was already called on this {@link ContainerBuilder} instance.
     * <p>
     * {@link #buildToTopBuilder()} called on a container that is not an immediate child of the top level builder
     *  (typically a {@link PageBuilder}) calls
     *
     * @return the {@link org.gatein.api.composition.PageBuilder} that started this {@link ContainerBuilder}.
     * @throws IllegalStateException if there is no reference to the top level builder
     * @throws IllegalStateException if {@link #buildToTopBuilder()} or {@link #buildToParentBuilder()}
     *                              was already called on this {@link ContainerBuilder} instance
     */
    public T buildToTopBuilder();

    /**
     * Returns a new instance of {@link Container} based on the provided information.
     * <b>
     * This method can be safely called multiple times on the same {@link ContainerBuilder} instance,
     * e.g. to produce {@link Container}s that differ in some small detail:
     * <pre>
     * ContainerBuilder myContainerBuilder = ...
     * Container c1 = myContainerBuilder
     *      .child(ch1)
     *      .child(ch2)
     *      .build();
     *
     * Container c2 = myContainerBuilder
     *      .child(ch3)
     *      .build();
     * // c2 contains 3 children: ch1, ch2 and ch3
     * </pre>
     *
     * @return new instance of {@link Container}
     */
    public Container build();

    /**
     * Starts a new child builder, using the column template. Children added to this new builder will be rendered as
     * columns on the screen.
     *
     * @return a newly created {@link ContainerBuilder}, specialized in rendering columns
     */
    public ContainerBuilder<T> newColumnsBuilder();

    /**
     * Starts a new child builder, using the row template. Children added to this new builder will be rendered as
     * rows on the screen.
     *
     * @return a newly created {@link ContainerBuilder}, specialized in rendering rows
     */
    public ContainerBuilder<T> newRowsBuilder();

    /**
     * Starts a new child builder, that builds on top of the provided Container. Useful when a custom container type is
     * required.
     * <p>
     * Please check the {@link ContainerBuilder}s provided by the API before falling back to this method. They can be
     * accessed through {@code new*Builder()} methods of {@link PageBuilder}
     * and {@link ContainerBuilder}.
     *
     * @return a newly created {@link ContainerBuilder}
     */
    public ContainerBuilder<T> newCustomContainerBuilder(Container container);

    /**
     * Starts a new child builder, that builds on top of the provided internal template URL,
     * using a generic container implementation.
     * <p>
     * Please check the {@link ContainerBuilder}s provided by the API before falling back to this method. They can be
     * accessed through {@code new*Builder()} methods of {@link PageBuilder}
     * and {@link ContainerBuilder}.
     * <p>
     * For more details on template URLs, refer to {@link Container#getTemplate()}
     * and {@link Container#setTemplate(String)}
     *
     * @param template internal template URL
     * @return a newly created {@link ContainerBuilder}
     *
     * @see Container#getTemplate()
     * @see Container#setTemplate(String)
     */
    public ContainerBuilder<T> newCustomContainerBuilder(String template);

    /**
     * Optionally sets the permission object that represents which users will be allowed to access the {@link Container} being built.
     * <p>
     * Unless set explicitly, the default value {@link Container#DEFAULT_ACCESS_PERMISSION} will be used for
     * the resulting {@link Container}.
     *
     * @param accessPermission the access permission for this container
     * @return this builder
     */
    public ContainerBuilder<T> accessPermission(Permission accessPermission);

    /**
     * Optionally sets the permission object that represents which users will be allowed to perform move, add
     * and remove operations with child {@link Application}s of the {@link Container} being built.
     * <p>
     * Unless set explicitly, the default value {@link Container#DEFAULT_MOVE_APPS_PERMISSION} will be used for
     * the resulting {@link Container}.
     *
     * @param moveAppsPermission the list of move apps permissions for this container
     * @return this builder
     */
    public ContainerBuilder<T> moveAppsPermission(Permission moveAppsPermission);

    /**
     * Optionally sets the permission object that represents which users will be allowed to perform move, add
     * and remove operations with child {@link Container}s of the {@link Container} being built.
     * <p>
     * Unless set explicitly, the default value {@link Container#DEFAULT_MOVE_CONTAINERS_PERMISSION} will be used for
     * the resulting {@link Container}.
     *
     * @param moveContainersPermission the list of move containers permissions for this container
     * @return this builder
     */
    public ContainerBuilder<T> moveContainersPermission(Permission moveContainersPermission);
}
