package com.grappim.taigamobile

import java.time.LocalDate

object TestData {
    val users = listOf(
        User(
            username = "test",
            password = "testing_password",
            email = "test@test.com",
            fullName = "Test Test"
        ),
        User(
            username = "another_test",
            password = "testing_password",
            email = "another_test@test.com",
            fullName = "Another Test"
        )
    )

    val activeUser = users[1]

    val projects = listOf(
        object : Project(
            name = "Cool Test Project",
            description = "And cool description",
            creator = users[0],
            team = users,
            defaultRoleName = "Testing role"
        ) {
            override val epics = listOf(
                Epic(
                    title = "Epic #1",
                    creator = team[0],
                    description = "Epic 1",
                    comments = listOf(
                        Comment("Important", team[0]),
                        Comment("Yes", team[1])
                    ),
                    assignedTo = team[0],
                    isClosed = true
                ),
                Epic(
                    title = "Epic #2",
                    creator = team[0],
                    watchers = team
                )
            )

            override val sprints = listOf(
                Sprint(
                    name = "Sprint 1",
                    start = LocalDate.of(2021, 9, 1),
                    end = LocalDate.of(2021, 9, 15),
                    creator = team[0]
                ),

                Sprint(
                    name = "Sprint 2",
                    start = LocalDate.of(2021, 9, 16),
                    end = LocalDate.of(2021, 9, 30),
                    creator = team[1],
                    tasks = listOf(
                        Task(
                            title = "Storyless task 1",
                            creator = team[1],
                            description = "Desc",
                            comments = listOf(
                                Comment("Some", team[0]),
                                Comment("Comment", team[1])
                            ),
                            assignedTo = team[1],
                            isClosed = true
                        ),
                        Task("Storyless task 2", team[0])
                    )
                )
            )

            override val userstories = listOf(
                UserStory(
                    title = "Cool user story",
                    creator = team[0],
                    description = "Very interesting description",
                    epics = epics,
                    comments = listOf(
                        Comment("Comment", team[0])
                    )
                ),
                UserStory(
                    title = "Another user story",
                    creator = team[1],
                    description = "Description",
                    assignedTo = team[0],
                    isClosed = true,
                    sprint = sprints[0],
                    tasks = listOf(
                        Task(
                            title = "Subtask 1",
                            creator = team[1],
                            assignedTo = team[0],
                            isClosed = true
                        ),
                        Task(
                            title = "Subtask 2",
                            creator = team[0],
                            isClosed = true
                        )
                    )
                ),
                UserStory(
                    title = "Great story",
                    creator = team[0],
                    description = "",
                    watchers = listOf(team[0]),
                    sprint = sprints[1],
                    tasks = listOf(
                        Task("Task #42", team[0], watchers = listOf(team[1])),
                        Task("Task #24", team[1])
                    )
                )
            )

            override val issues = listOf(
                Issue(
                    title = "Issue #1",
                    creator = team[1],
                    description = "Problem #1",
                    assignedTo = team[0],
                    isClosed = true,
                    sprint = sprints[0]
                ),
                Issue(
                    title = "Issue #2",
                    creator = team[1],
                    description = "Problem #2",
                    sprint = sprints[1]
                ),
                Issue(
                    title = "Issue #3",
                    creator = team[0],
                    description = "Problem #3"
                )
            )
        },
        object : Project(
            name = "Cool Test Project",
            description = "And cool description",
            creator = users[1],
            team = users,
            defaultRoleName = "Testing role"
        ) {
            override val epics = listOf(
                Epic(
                    title = "Epic #1",
                    creator = team[1],
                    description = "Epic 1",
                    comments = listOf(
                        Comment("Important", team[1]),
                        Comment("Yes", team[0])
                    ),
                    assignedTo = team[1],
                    isClosed = true
                ),
                Epic(
                    title = "Epic #2",
                    creator = team[0],
                    watchers = team
                )
            )

            override val sprints = listOf(
                Sprint(
                    name = "Sprint 1",
                    start = LocalDate.of(2021, 9, 1),
                    end = LocalDate.of(2021, 9, 15),
                    creator = team[1]
                ),

                Sprint(
                    name = "Sprint 2",
                    start = LocalDate.of(2021, 9, 16),
                    end = LocalDate.of(2021, 9, 30),
                    creator = team[0],
                    tasks = listOf(
                        Task(
                            title = "Storyless task 1",
                            creator = team[0],
                            description = "Desc",
                            comments = listOf(
                                Comment("Some", team[1]),
                                Comment("Comment", team[0])
                            ),
                            assignedTo = team[0],
                            isClosed = true
                        ),
                        Task("Storyless task 2", team[1])
                    )
                )
            )

            override val userstories = listOf(
                UserStory(
                    title = "Cool user story",
                    creator = team[1],
                    description = "Very interesting description",
                    epics = epics,
                    comments = listOf(
                        Comment("Comment", team[1])
                    )
                ),
                UserStory(
                    title = "Another user story",
                    creator = team[0],
                    description = "Description",
                    assignedTo = team[1],
                    isClosed = true,
                    sprint = sprints[0],
                    tasks = listOf(
                        Task(
                            title = "Subtask 1",
                            creator = team[1],
                            assignedTo = team[1],
                            isClosed = true
                        ),
                        Task(
                            title = "Subtask 2",
                            creator = team[0],
                            isClosed = true
                        )
                    )
                ),
                UserStory(
                    title = "Great story",
                    creator = team[1],
                    description = "",
                    watchers = listOf(team[1]),
                    sprint = sprints[1],
                    tasks = listOf(
                        Task("Task #42", team[0], watchers = listOf(team[0])),
                        Task("Task #24", team[1])
                    )
                )
            )

            override val issues = listOf(
                Issue(
                    title = "Issue #1",
                    creator = team[1],
                    description = "Problem #1",
                    assignedTo = team[1],
                    isClosed = true,
                    sprint = sprints[0]
                ),
                Issue(
                    title = "Issue #2",
                    creator = team[0],
                    description = "Problem #2",
                    sprint = sprints[1]
                ),
                Issue(
                    title = "Issue #3",
                    creator = team[0],
                    description = "Problem #3"
                )
            )
        }
    )
}
